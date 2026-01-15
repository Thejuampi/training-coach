package com.training.coach.shared.domain.unit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record Miles(double value) implements Distance {
    public Miles {
        if (value < 0) {
            throw new IllegalArgumentException("Miles must be non-negative");
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Miles of(double value) {
        return new Miles(value);
    }

    @Override
    public Meters toMeters() {
        return new Meters(value * 1609.344);
    }

    @Override
    public Kilometers toKilometers() {
        return new Kilometers(value * 1.609344);
    }

    @Override
    public Miles toMiles() {
        return this;
    }

    @JsonValue
    public double value() {
        return value;
    }
}
