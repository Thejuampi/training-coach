package com.training.coach.shared.domain.unit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record Meters(double value) implements Distance {
    public Meters {
        if (value < 0) {
            throw new IllegalArgumentException("Meters must be non-negative");
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Meters of(double value) {
        return new Meters(value);
    }

    @Override
    public Meters toMeters() {
        return this;
    }

    @Override
    public Kilometers toKilometers() {
        return new Kilometers(value / 1000.0);
    }

    @Override
    public Miles toMiles() {
        return new Miles(value / 1609.344);
    }

    @JsonValue
    public double value() {
        return value;
    }
}
