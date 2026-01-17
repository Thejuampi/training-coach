package com.training.coach.wellness.domain.model;

public record TrainingLoadSummary(double tss, double ctl, double atl, double tsb, int trainingMinutes) {
    public TrainingLoadSummary {
        if (tss < 0) {
            throw new IllegalArgumentException("TSS must be non-negative");
        }
        if (ctl < 0) {
            throw new IllegalArgumentException("CTL must be non-negative");
        }
        if (atl < 0) {
            throw new IllegalArgumentException("ATL must be non-negative");
        }
        if (trainingMinutes < 0) {
            throw new IllegalArgumentException("Training minutes must be non-negative");
        }
    }

    public static TrainingLoadSummary empty() {
        return new TrainingLoadSummary(0.0, 0.0, 0.0, 0.0, 0);
    }

    public boolean hasTrainingData() {
        return tss > 0 || trainingMinutes > 0;
    }

    public String recoveryStatus() {
        if (tsb > 15) return "optimal";
        if (tsb > 0) return "good";
        if (tsb > -15) return "moderate";
        return "high_load";
    }
}
