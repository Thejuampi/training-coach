package com.training.coach.shared.domain.unit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record Grams(double value) implements Weight {
    public Grams {
        if (value < 0) {
            throw new IllegalArgumentException("Grams must be non-negative");
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Grams of(double value) {
        return new Grams(value);
    }

    @Override
    public Kilograms toKilograms() {
        return new Kilograms(value / 1000.0);
    }

    @Override
    public Grams toGrams() {
        return this;
    }

    @Override
    public Pounds toPounds() {
        return new Pounds(value / 453.59237);
    }

    @JsonValue
    public double value() {
        return value;
    }
}
