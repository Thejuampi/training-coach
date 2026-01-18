package com.training.coach.shared.domain.unit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Heart rate variability (typically ms).
 */
public record HeartRateVariability(double value) {
    public HeartRateVariability {
        if (value < 0) {
            throw new IllegalArgumentException("HRV must be non-negative");
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static HeartRateVariability of(double value) {
        return new HeartRateVariability(value);
    }

    @JsonValue
    public double value() {
        return value;
    }
}