package com.training.coach.shared.domain.unit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record Percent(double value) {
    public Percent {
        if (value < 0 || value > 100.0) {
            throw new IllegalArgumentException("Percent must be between 0 and 100");
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Percent of(double value) {
        return new Percent(value);
    }

    @JsonValue
    public double value() {
        return value;
    }
}
