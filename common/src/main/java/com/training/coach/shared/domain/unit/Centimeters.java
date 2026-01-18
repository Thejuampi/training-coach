package com.training.coach.shared.domain.unit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record Centimeters(double value) implements Length {
    public Centimeters {
        if (value < 0) {
            throw new IllegalArgumentException("Centimeters must be non-negative");
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Centimeters of(double value) {
        return new Centimeters(value);
    }

    @Override
    public Centimeters toCentimeters() {
        return this;
    }

    @Override
    public Inches toInches() {
        return new Inches(value / 2.54);
    }

    @JsonValue
    public double value() {
        return value;
    }
}