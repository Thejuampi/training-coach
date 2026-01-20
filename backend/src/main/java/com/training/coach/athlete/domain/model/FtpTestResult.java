package com.training.coach.athlete.domain.model;

import com.training.coach.shared.domain.unit.Watts;
import java.time.LocalDate;

/**
 * Immutable record representing an FTP test result with method and confidence.
 */
public record FtpTestResult(
        Watts ftp,
        LocalDate testDate,
        TestMethod method,
        double confidencePercent
) {
    public enum TestMethod {
        LAB_LACTATE,      // Laboratory lactate test
        FIELD_RAMP,       // Field test with ramp protocol
        FIELD_20MIN,      // 20-minute power test
        ESTIMATED         // Estimate from other metrics
    }

    public FtpTestResult {
        if (ftp == null) {
            throw new IllegalArgumentException("FTP cannot be null");
        }
        if (ftp.value() <= 0) {
            throw new IllegalArgumentException("FTP must be positive");
        }
        if (testDate == null) {
            throw new IllegalArgumentException("Test date cannot be null");
        }
        if (method == null) {
            throw new IllegalArgumentException("Method cannot be null");
        }
        if (confidencePercent < 0 || confidencePercent > 100) {
            throw new IllegalArgumentException("Confidence must be 0-100");
        }
    }
}
