package com.training.coach.shared.domain.unit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record Pounds(double value) implements Weight {
    public Pounds {
        if (value < 0) {
            throw new IllegalArgumentException("Pounds must be non-negative");
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Pounds of(double value) {
        return new Pounds(value);
    }

    @Override
    public Kilograms toKilograms() {
        return new Kilograms(value / 2.2046226218);
    }

    @Override
    public Grams toGrams() {
        return new Grams(value * 453.59237);
    }

    @Override
    public Pounds toPounds() {
        return this;
    }

    @JsonValue
    public double value() {
        return value;
    }
}