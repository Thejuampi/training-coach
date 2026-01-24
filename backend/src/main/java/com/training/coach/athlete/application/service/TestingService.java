package com.training.coach.athlete.application.service;

import com.training.coach.athlete.application.port.out.AthleteRepository;
import com.training.coach.athlete.application.port.out.TestResultRepository;
import com.training.coach.athlete.domain.model.FtpTestResult;
import com.training.coach.athlete.domain.model.TrainingMetrics;
import com.training.coach.shared.domain.unit.Watts;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Application service for scheduling and managing athlete testing protocols
 * including FTP tests, threshold tests, and other performance assessments.
 */
@Service
public class TestingService {

    private static final Logger logger = LoggerFactory.getLogger(TestingService.class);

    private final AthleteRepository athleteRepository;
    private final FtpTestService ftpTestService;
    private final TestResultRepository testResultRepository;

    private final Map<String, List<ScheduledTest>> scheduledTests = new HashMap<>();

    public TestingService(
            AthleteRepository athleteRepository,
            FtpTestService ftpTestService,
            TestResultRepository testResultRepository) {
        this.athleteRepository = athleteRepository;
        this.ftpTestService = ftpTestService;
        this.testResultRepository = testResultRepository;
    }

    /**
     * Schedule an FTP test for an athlete on a specific date.
     */
    public void scheduleFtpTest(String athleteId, LocalDate date) {
        var athlete = athleteRepository.findById(athleteId)
                .orElseThrow(() -> new IllegalArgumentException("Athlete not found: " + athleteId));

        scheduledTests.computeIfAbsent(athleteId, k -> new ArrayList<>())
                .add(new ScheduledTest(date, TestType.FTP_TEST, TestStatus.SCHEDULED));

        logger.info("Scheduled FTP test for athlete {} on {}", athleteId, date);
    }

    /**
     * Record FTP test result for an athlete.
     * This will update the athlete's FTP and recalculate all zones.
     */
    public FtpTestResult recordFtpTestResult(String athleteId, LocalDate testDate, double ftpValue) {
        // Validate the FTP test result
        if (ftpValue <= 0) {
            throw new IllegalArgumentException("FTP must be positive");
        }

        var athlete = athleteRepository.findById(athleteId)
                .orElseThrow(() -> new IllegalArgumentException("Athlete not found: " + athleteId));

        // Determine test method (this would come from test configuration in a real implementation)
        FtpTestResult.TestMethod method = FtpTestResult.TestMethod.FIELD_20MIN;

        // Calculate confidence based on method (simplified for now)
        double confidence = switch (method) {
            case LAB_LACTATE -> 95.0;
            case FIELD_RAMP, FIELD_20MIN -> 85.0;
            case ESTIMATED -> 70.0;
        };

        // Create FTP test result
        FtpTestResult testResult = new FtpTestResult(
                Watts.of(ftpValue),
                testDate,
                method,
                confidence
        );

        // Update athlete's FTP and recalculate zones using FtpTestService
        var updatedAthlete = ftpTestService.processFtpTest(athleteId, testResult);

        // Store the test result
        testResultRepository.save(testResult);

        // Update test status
        updateTestStatus(athleteId, testDate, TestStatus.COMPLETED);

        logger.info("Recorded FTP test result {} for athlete {}", ftpValue, athleteId);
        return testResult;
    }

    /**
     * Get test instructions for executing an FTP test.
     */
    public String getTestInstructions(TestType testType) {
        return switch (testType) {
            case FTP_TEST -> """
                FTP Test Instructions:
                1. Warm up for 15-20 minutes at easy pace (Zone 2)
                2. Complete a 20-minute time trial at maximum sustainable effort
                3. Maintain consistent power output throughout
                4. Record your average power for the 20 minutes
                5. Your FTP will be calculated as 95% of your 20-minute average power
                6. Cool down for 10-15 minutes at easy pace
                """;
            case THRESHOLD -> """
                Threshold Test Instructions:
                1. Warm up for 20 minutes including some intervals at threshold pace
                2. Sustain 60 minutes at your threshold effort (comfortably hard)
                3. Record your average heart rate and power
                4. Cool down for 15 minutes easy
                """;
            case VO2_MAX -> """
                VO2 Max Test Instructions:
                1. Perform a thorough 20-minute warmup
                2. The test starts easy and increases in intensity every minute
                3. Continue until exhaustion
                4. Your VO2 max will be estimated from the power/heart rate data
                """;
        };
    }

    /**
     * Get all scheduled tests for an athlete.
     */
    public List<ScheduledTest> getScheduledTests(String athleteId) {
        return scheduledTests.getOrDefault(athleteId, List.of());
    }

    /**
     * Get scheduled test for a specific date.
     */
    public ScheduledTest getTestForDate(String athleteId, LocalDate date) {
        return scheduledTests.getOrDefault(athleteId, List.of())
                .stream()
                .filter(t -> t.date().equals(date))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get all historical test results for an athlete.
     */
    public List<FtpTestResult> getHistoricalTestResults(String athleteId) {
        return testResultRepository.findByAthleteId(athleteId);
    }

    /**
     * Get the latest FTP test result for an athlete.
     */
    public Optional<FtpTestResult> getLatestFtpTestResult(String athleteId) {
        return testResultRepository.findByAthleteId(athleteId).stream()
                .max(java.util.Comparator.comparing(FtpTestResult::testDate));
    }

    /**
     * Check if an athlete needs to recalculate zones after an FTP update.
     * Returns true if there are future workouts that should reflect the new FTP.
     */
    public boolean needsZoneRecalculation(String athleteId, LocalDate ftpUpdateDate) {
        // In a real implementation, this would check for future workouts
        // For now, assume true to trigger recalculation
        return true;
    }

    /**
     * Update test status in the schedule.
     */
    private void updateTestStatus(String athleteId, LocalDate testDate, TestStatus status) {
        scheduledTests.computeIfAbsent(athleteId, k -> new ArrayList<>())
                .removeIf(t -> t.date().equals(testDate));
        scheduledTests.get(athleteId).add(new ScheduledTest(testDate, TestType.FTP_TEST, status));
    }

    /**
     * Record for athlete FTP test history.
     */
    public record TestHistoryRecord(
            String athleteId,
            double ftpValue,
            LocalDate testDate,
            FtpTestResult.TestMethod testMethod,
            double confidencePercent
    ) {}

    public record ScheduledTest(
            LocalDate date,
            TestType type,
            TestStatus status
    ) {}

    public enum TestType {
        FTP_TEST,
        THRESHOLD,
        VO2_MAX
    }

    public enum TestStatus {
        SCHEDULED,
        COMPLETED,
        CANCELLED
    }
}
