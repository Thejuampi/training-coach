package com.training.coach.athlete.application.service;

import com.training.coach.analysis.application.service.SeilerThresholds;
import com.training.coach.analysis.application.service.SeilerThresholdService;
import com.training.coach.analysis.application.service.SeilerZone;
import com.training.coach.athlete.domain.model.PrescriptionBand;
import com.training.coach.shared.domain.unit.Watts;
import com.training.coach.shared.domain.unit.WattsRange;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Application service for calculating training zones from athlete metrics.
 * Uses Seiler's zone model based on LT1 and LT2 thresholds.
 */
@Service
public class ZoneCalculationService {

    private final SeilerThresholdService seilerThresholdService;

    public ZoneCalculationService(SeilerThresholdService seilerThresholdService) {
        this.seilerThresholdService = seilerThresholdService;
    }

    /**
     * Calculate prescription bands for a given FTP value.
     *
     * @param ftp the athlete's current FTP
     * @param method the testing method used (affects confidence)
     * @param confidence the confidence level (0-100)
     * @return list of prescription bands for all zones
     */
    public List<PrescriptionBand> calculateBandsFromFtp(Watts ftp, String method, double confidence) {
        Map<SeilerZone, WattsRange> ranges = calculateZoneRanges(ftp);

        return ranges.entrySet().stream()
                .map(entry -> new PrescriptionBand(
                        entry.getKey(),
                        entry.getValue(),
                        method,
                        confidence
                ))
                .toList();
    }

    /**
     * Calculate zone ranges based on FTP.
     * Uses Seiler's 3-zone model:
     * - Zone 1 (Active Recovery): Below LT1
     * - Zone 2 (Tempo/Aerobic): Between LT1 and LT2
     * - Zone 3 (Threshold/Anaerobic): Above LT2
     */
    public Map<SeilerZone, WattsRange> calculateZoneRanges(Watts ftp) {
        Map<SeilerZone, WattsRange> ranges = new HashMap<>();

        // LT1 is typically at ~81-84% of FTP (Seiler's research)
        // LT2 is at FTP (functional threshold power)
        Watts lt1 = Watts.of(ftp.value() * 0.82);
        Watts lt2 = ftp;

        // Zone 1: Below LT1 (active recovery)
        ranges.put(SeilerZone.Z1, WattsRange.of(0, lt1.value()));

        // Zone 2: Between LT1 and LT2 (tempo/aerobic development)
        ranges.put(SeilerZone.Z2, WattsRange.of(lt1.value(), lt2.value()));

        // Zone 3: Above LT2 (threshold and above)
        ranges.put(SeilerZone.Z3, WattsRange.of(lt2.value(), ftp.value() * 1.15));

        return ranges;
    }

    /**
     * Get prescription bands for an athlete based on their stored thresholds.
     *
     * @param athleteId the athlete's ID
     * @return list of prescription bands for all zones
     */
    public List<PrescriptionBand> getBandsForAthlete(String athleteId) {
        return seilerThresholdService.getForAthlete(athleteId)
                .map(thresholds -> {
                    String method = thresholds.method().name();
                    double confidence = thresholds.confidence() * 100;
                    return calculateBandsFromFtp(thresholds.lt2Watts(), method, confidence);
                })
                .orElse(List.of());
    }

    /**
     * Calculate Seiler 3-zone boundaries from thresholds.
     * Returns the boundary values between zones.
     *
     * @param thresholds the athlete's threshold values
     * @return map of zone boundaries
     */
    public Map<String, Watts> calculateZoneBoundaries(SeilerThresholds thresholds) {
        Map<String, Watts> boundaries = new HashMap<>();
        boundaries.put("Z1_Z2_boundary", thresholds.lt1Watts());
        boundaries.put("Z2_Z3_boundary", thresholds.lt2Watts());
        return boundaries;
    }

    /**
     * Update prescription bands when new thresholds are available.
     *
     * @param thresholds the new threshold values
     * @return updated prescription bands
     */
    public List<PrescriptionBand> recalculateBands(SeilerThresholds thresholds) {
        String method = thresholds.method().name();
        double confidence = thresholds.confidence() * 100;
        return calculateBandsFromFtp(thresholds.lt2Watts(), method, confidence);
    }
}
