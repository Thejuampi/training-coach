package com.training.coach.athlete.application.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Application service for scheduling and managing athlete testing protocols
 * including FTP tests, threshold tests, and other performance assessments.
 */
@Service
public class TestingService {

    private final Map<String, List<ScheduledTest>> scheduledTests = new HashMap<>();
    private final Map<String, Double> ftpResults = new HashMap<>();

    /**
     * Schedule an FTP ramp on a specific date test for an athlete.
     */
    public void scheduleFtpTest(String athleteId, LocalDate date) {
        scheduledTests.computeIfAbsent(athleteId, k -> new ArrayList<>())
            .add(new ScheduledTest(date, TestType.FTP_RAMP, TestStatus.SCHEDULED));
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
     * Get test instructions for executing an FTP test.
     */
    public String getTestInstructions(TestType testType) {
        return switch (testType) {
            case FTP_RAMP -> """
                FTP Ramp Test Instructions:
                1. Warm up for 15-20 minutes at easy pace
                2. Start the test at 100 watts and increase by 20 watts every 4 minutes
                3. Maintain consistent effort until you can no longer hold the pace
                4. Cool down for 10-15 minutes after the test
                5. Your FTP will be calculated as 75% of the average power of the last 20 minutes
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
     * Record the result of an FTP test and update athlete metrics.
     * Returns the new FTP value.
     */
    public double recordFtpTestResult(String athleteId, LocalDate testDate, double ftpResult) {
        // Store the actual FTP result
        ftpResults.put(athleteId + "_" + testDate.toString(), ftpResult);
        
        // Update the scheduled test status
        scheduledTests.computeIfAbsent(athleteId, k -> new ArrayList<>())
            .removeIf(t -> t.date().equals(testDate) && t.type() == TestType.FTP_RAMP);
        scheduledTests.get(athleteId).add(new ScheduledTest(testDate, TestType.FTP_RAMP, TestStatus.COMPLETED));
        
        return ftpResult;
    }

    /**
     * Get the latest FTP result for an athlete.
     */
    public Double getLatestFtpResult(String athleteId) {
        return scheduledTests.getOrDefault(athleteId, List.of())
            .stream()
            .filter(t -> t.type() == TestType.FTP_RAMP && t.status() == TestStatus.COMPLETED)
            .max(java.util.Comparator.comparing(ScheduledTest::date))
            .map(t -> ftpResults.get(athleteId + "_" + t.date().toString()))
            .orElse(null);
    }

    public record ScheduledTest(
        LocalDate date,
        TestType type,
        TestStatus status
    ) {}

    public enum TestType {
        FTP_RAMP,
        THRESHOLD,
        VO2_MAX
    }

    public enum TestStatus {
        SCHEDULED,
        COMPLETED,
        CANCELLED
    }
}
