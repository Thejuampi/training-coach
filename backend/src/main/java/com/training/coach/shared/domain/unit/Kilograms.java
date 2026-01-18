package com.training.coach.shared.domain.unit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record Kilograms(double value) implements Weight {
    public Kilograms {
        if (value < 0) {
            throw new IllegalArgumentException("Kilograms must be non-negative");
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Kilograms of(double value) {
        return new Kilograms(value);
    }

    @Override
    public Kilograms toKilograms() {
        return this;
    }

    @Override
    public Grams toGrams() {
        return new Grams(value * 1000.0);
    }

    @Override
    public Pounds toPounds() {
        return new Pounds(value * 2.2046226218);
    }

    @JsonValue
    public double value() {
        return value;
    }
}
