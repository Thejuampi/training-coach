package com.training.coach.shared.domain.unit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record BeatsPerMinute(double value) implements HeartRate {
    public BeatsPerMinute {
        if (value < 0) {
            throw new IllegalArgumentException("Beats per minute must be non-negative");
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static BeatsPerMinute of(double value) {
        return new BeatsPerMinute(value);
    }

    @Override
    public BeatsPerMinute toBeatsPerMinute() {
        return this;
    }

    @JsonValue
    public double value() {
        return value;
    }
}