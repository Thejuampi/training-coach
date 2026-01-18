package com.training.coach.shared.domain.unit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record Kilowatts(double value) implements Power {
    public Kilowatts {
        if (value < 0) {
            throw new IllegalArgumentException("Kilowatts must be non-negative");
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Kilowatts of(double value) {
        return new Kilowatts(value);
    }

    @Override
    public Watts toWatts() {
        return new Watts(value * 1000.0);
    }

    @Override
    public Kilowatts toKilowatts() {
        return this;
    }

    @JsonValue
    public double value() {
        return value;
    }
}