package com.training.coach.shared.domain.unit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record Kilometers(double value) implements Distance {
    public Kilometers {
        if (value < 0) {
            throw new IllegalArgumentException("Kilometers must be non-negative");
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Kilometers of(double value) {
        return new Kilometers(value);
    }

    public static Kilometers fromMeters(double meters) {
        return new Kilometers(meters / 1000.0);
    }

    @Override
    public Meters toMeters() {
        return new Meters(value * 1000.0);
    }

    @Override
    public Kilometers toKilometers() {
        return this;
    }

    @Override
    public Miles toMiles() {
        return new Miles(value / 1.609344);
    }

    @JsonValue
    public double value() {
        return value;
    }
}
