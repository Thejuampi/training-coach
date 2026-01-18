package com.training.coach.wellness.domain.model;

public record WellnessTrends(
        TrendDirection rhrTrend,
        TrendDirection hrvTrend,
        TrendDirection bodyWeightTrend,
        TrendDirection sleepHoursTrend,
        TrendDirection readinessTrend,
        TrendDirection tssTrend,
        double averageReadinessScore,
        double readinessVariance,
        double averageHrv,
        double averageRhr,
        double averageSleepHours) {
    public WellnessTrends {
        if (averageReadinessScore < 0 || averageReadinessScore > 100) {
            throw new IllegalArgumentException("Average readiness score must be between 0 and 100");
        }
        if (readinessVariance < 0) {
            throw new IllegalArgumentException("Readiness variance cannot be negative");
        }
    }

    public enum TrendDirection {
        INSUFFICIENT_DATA,
        STABLE,
        SLIGHT_IMPROVEMENT,
        IMPROVING,
        DECLINING,
        SLIGHT_DECLINE
    }

    public static WellnessTrends empty() {
        return new WellnessTrends(
                TrendDirection.INSUFFICIENT_DATA,
                TrendDirection.INSUFFICIENT_DATA,
                TrendDirection.INSUFFICIENT_DATA,
                TrendDirection.INSUFFICIENT_DATA,
                TrendDirection.INSUFFICIENT_DATA,
                TrendDirection.INSUFFICIENT_DATA,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0);
    }

    public boolean hasSufficientData() {
        return rhrTrend != TrendDirection.INSUFFICIENT_DATA;
    }
}
