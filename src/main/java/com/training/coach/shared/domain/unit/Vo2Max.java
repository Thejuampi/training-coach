package com.training.coach.shared.domain.unit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * VO2 Max in ml/kg/min.
 */
public record Vo2Max(double value) {
    public Vo2Max {
        if (value < 0) {
            throw new IllegalArgumentException("VO2 max must be non-negative");
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Vo2Max of(double value) {
        return new Vo2Max(value);
    }

    @JsonValue
    public double value() {
        return value;
    }
}
