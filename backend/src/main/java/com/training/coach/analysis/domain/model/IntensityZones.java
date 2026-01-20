package com.training.coach.analysis.domain.model;

import com.training.coach.shared.domain.unit.Watts;

/**
 * Intensity zones derived from LT1 and LT2 thresholds.
 * Defines the boundaries between Z1, Z2, and Z3 training zones.
 */
public record IntensityZones(
        Watts lt1,
        Watts lt2,
        Watts z1UpperBound,
        Watts z2UpperBound,
        Watts z3UpperBound) {
    public IntensityZones {
        if (lt1.value() <= 0) {
            throw new IllegalArgumentException("LT1 must be positive");
        }
        if (lt2.value() <= 0) {
            throw new IllegalArgumentException("LT2 must be positive");
        }
        if (lt2.value() <= lt1.value()) {
            throw new IllegalArgumentException("LT2 must be greater than LT1");
        }
    }

    /**
     * Classifies a power value into the appropriate zone.
     *
     * @param power the power to classify
     * @return the zone (Z1, Z2, or Z3)
     */
    public Zone classify(Watts power) {
        if (power.value() <= z1UpperBound.value()) {
            return Zone.Z1;
        }
        if (power.value() <= z2UpperBound.value()) {
            return Zone.Z2;
        }
        return Zone.Z3;
    }

    /**
     * Returns true if the given power is below LT1 (FATMAX zone).
     */
    public boolean isBelowLt1(Watts power) {
        return power.value() < lt1.value();
    }

    /**
     * Returns true if the given power is between LT1 and LT2.
     */
    public boolean isBetweenLt1AndLt2(Watts power) {
        return power.value() >= lt1.value() && power.value() < lt2.value();
    }

    /**
     * Returns true if the given power is at or above LT2.
     */
    public boolean isAtOrAboveLt2(Watts power) {
        return power.value() >= lt2.value();
    }
}
