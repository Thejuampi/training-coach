package com.training.coach.analysis.domain.model;

/**
 * Represents the distribution of training time across intensity zones.
 * Used to analyze workout profiles and detect training patterns.
 */
public record PolarizedDistribution(
        double z1Percent,
        double z2Percent,
        double z3Percent) {
    public PolarizedDistribution {
        // Normalize percentages to handle rounding
        double total = z1Percent + z2Percent + z3Percent;
        if (total > 0 && Math.abs(total - 100.0) > 0.01) {
            z1Percent = (z1Percent / total) * 100.0;
            z2Percent = (z2Percent / total) * 100.0;
            z3Percent = (z3Percent / total) * 100.0;
        }
    }

    /**
     * Returns true if the distribution follows Seiler's polarized model.
     * Polarized: ~80% Z1, ~10% Z2, ~10% Z3
     */
    public boolean isPolarized() {
        return z1Percent >= 75.0 && z3Percent >= 15.0 && z2Percent <= 10.0;
    }

    /**
     * Returns true if zone 2 training exceeds the recommended threshold.
     * Z2 creep indicates excessive moderate-intensity training.
     */
    public boolean hasZ2Creep() {
        return z2Percent > 20.0;
    }

    /**
     * Returns true if the distribution is tempo-heavy.
     * Tempo: Excessive Z2, minimal Z3
     */
    public boolean isTempoHeavy() {
        return z2Percent > 25.0 && z3Percent < 10.0;
    }

    /**
     * Returns true if the distribution is threshold-focused.
     * Threshold: Moderate Z1 and Z3, high Z2
     */
    public boolean isThresholdFocus() {
        return z2Percent > 20.0 && z2Percent <= 40.0 && z3Percent >= 10.0;
    }

    /**
     * Creates a distribution from zone minutes.
     */
    public static PolarizedDistribution fromMinutes(double z1Minutes, double z2Minutes, double z3Minutes) {
        double total = z1Minutes + z2Minutes + z3Minutes;
        if (total == 0) {
            return new PolarizedDistribution(0.0, 0.0, 0.0);
        }
        return new PolarizedDistribution(
                (z1Minutes / total) * 100.0,
                (z2Minutes / total) * 100.0,
                (z3Minutes / total) * 100.0
        );
    }
}
