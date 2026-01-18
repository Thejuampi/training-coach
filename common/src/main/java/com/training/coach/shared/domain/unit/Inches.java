package com.training.coach.shared.domain.unit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record Inches(double value) implements Length {
    public Inches {
        if (value < 0) {
            throw new IllegalArgumentException("Inches must be non-negative");
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Inches of(double value) {
        return new Inches(value);
    }

    @Override
    public Centimeters toCentimeters() {
        return new Centimeters(value * 2.54);
    }

    @Override
    public Inches toInches() {
        return this;
    }

    @JsonValue
    public double value() {
        return value;
    }
}