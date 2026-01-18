package com.training.coach.wellness.domain.model;

import com.training.coach.shared.domain.unit.Hours;

public record SleepMetrics(Hours totalSleepHours, int qualityScore, Hours deepSleepHours, Hours remSleepHours) {
    public SleepMetrics {
        if (totalSleepHours != null && totalSleepHours.value() < 0) {
            throw new IllegalArgumentException("Total sleep hours must be non-negative");
        }
        if (qualityScore < 1 || qualityScore > 10) {
            throw new IllegalArgumentException("Sleep quality score must be between 1 and 10");
        }
        if (deepSleepHours != null && deepSleepHours.value() < 0) {
            throw new IllegalArgumentException("Deep sleep hours must be non-negative");
        }
        if (remSleepHours != null && remSleepHours.value() < 0) {
            throw new IllegalArgumentException("REM sleep hours must be non-negative");
        }
    }

    public static SleepMetrics basic(Hours totalHours, int qualityScore) {
        return new SleepMetrics(totalHours, qualityScore, null, null);
    }
}
