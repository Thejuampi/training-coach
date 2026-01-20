package com.training.coach.athlete.application.service;

import com.training.coach.analysis.application.service.SeilerThresholdService;
import com.training.coach.analysis.application.service.SeilerThresholds;
import com.training.coach.analysis.application.service.ThresholdMethod;
import com.training.coach.athlete.application.port.out.AthleteRepository;
import com.training.coach.athlete.domain.model.Athlete;
import com.training.coach.athlete.domain.model.FtpTestResult;
import com.training.coach.athlete.domain.model.TrainingMetrics;
import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.Watts;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

/**
 * Application service for processing FTP test results and updating athlete zones.
 */
@Service
public class FtpTestService {

    private final AthleteRepository athleteRepository;
    private final SeilerThresholdService seilerThresholdService;
    private final ZoneCalculationService zoneCalculationService;

    public FtpTestService(
            AthleteRepository athleteRepository,
            SeilerThresholdService seilerThresholdService,
            ZoneCalculationService zoneCalculationService) {
        this.athleteRepository = athleteRepository;
        this.seilerThresholdService = seilerThresholdService;
        this.zoneCalculationService = zoneCalculationService;
    }

    /**
     * Process an FTP test result for an athlete.
     * Updates the athlete's FTP, recalculates zones, and stores the test result.
     *
     * @param athleteId the athlete's ID
     * @param testResult the FTP test result
     * @return the updated athlete with new metrics
     */
    public Athlete processFtpTest(String athleteId, FtpTestResult testResult) {
        Athlete athlete = athleteRepository.findById(athleteId)
                .orElseThrow(() -> new IllegalArgumentException("Athlete not found: " + athleteId));

        // Update athlete's FTP
        TrainingMetrics currentMetrics = athlete.currentMetrics();
        TrainingMetrics updatedMetrics = new TrainingMetrics(
                testResult.ftp(),
                currentMetrics.fthr(),
                currentMetrics.vo2max(),
                currentMetrics.weightKg()
        );
        Athlete updatedAthlete = athlete.withUpdatedMetrics(updatedMetrics);

        // Calculate and store new thresholds
        SeilerThresholds newThresholds = calculateThresholdsFromFtp(athleteId, testResult);
        seilerThresholdService.upsert(newThresholds);

        // Save the updated athlete
        return athleteRepository.save(updatedAthlete);
    }

    /**
     * Calculate Seiler thresholds from an FTP test result.
     */
    private SeilerThresholds calculateThresholdsFromFtp(String athleteId, FtpTestResult testResult) {
        // Convert FtpTestResult.TestMethod to ThresholdMethod
        ThresholdMethod method = switch (testResult.method()) {
            case LAB_LACTATE -> ThresholdMethod.LAB_LACTATE;
            case FIELD_RAMP, FIELD_20MIN -> ThresholdMethod.FIELD_PROXY;
            case ESTIMATED -> ThresholdMethod.ESTIMATED;
        };

        // Calculate LT1 and LT2 from FTP
        // LT2 is typically at or slightly below FTP
        // LT1 is typically at ~81-84% of LT2 (Seiler's findings)
        Watts ftp = testResult.ftp();
        Watts lt2Watts = ftp;
        Watts lt1Watts = Watts.of(ftp.value() * 0.82); // 82% of FTP for LT1

        // Estimate heart rate thresholds (rough approximation based on typical ratios)
        // These would be refined with lab tests or field calibrations
        BeatsPerMinute estimatedFthr = currentFthrEstimate(athleteId);
        BeatsPerMinute lt2Bpm = estimatedFthr;
        BeatsPerMinute lt1Bpm = BeatsPerMinute.of(estimatedFthr.value() * 0.88); // ~88% of FTHR

        return new SeilerThresholds(
                athleteId,
                lt1Watts,
                lt2Watts,
                lt1Bpm,
                lt2Bpm,
                testResult.testDate(),
                method,
                testResult.confidencePercent() / 100.0
        );
    }

    /**
     * Get the current FTHR estimate for an athlete, or default if not available.
     */
    private BeatsPerMinute currentFthrEstimate(String athleteId) {
        // In a real implementation, this would look up historical data
        // For now, return a default value that would typically come from the athlete's current metrics
        return BeatsPerMinute.of(170.0);
    }

    /**
     * Get the athlete's current FTP from their stored metrics.
     */
    public Watts getCurrentFtp(String athleteId) {
        Athlete athlete = athleteRepository.findById(athleteId)
                .orElseThrow(() -> new IllegalArgumentException("Athlete not found: " + athleteId));
        return athlete.currentMetrics().ftp();
    }

    /**
     * Check if an athlete has a recorded FTP.
     */
    public boolean hasFtp(String athleteId) {
        try {
            Watts ftp = getCurrentFtp(athleteId);
            return ftp.value() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
