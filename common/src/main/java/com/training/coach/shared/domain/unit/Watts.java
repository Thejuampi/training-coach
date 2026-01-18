package com.training.coach.shared.domain.unit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record Watts(double value) implements Power {
    public Watts {
        if (value < 0) {
            throw new IllegalArgumentException("Watts must be non-negative");
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Watts of(double value) {
        return new Watts(value);
    }

    @Override
    public Watts toWatts() {
        return this;
    }

    @Override
    public Kilowatts toKilowatts() {
        return new Kilowatts(value / 1000.0);
    }

    @JsonValue
    public double value() {
        return value;
    }
}