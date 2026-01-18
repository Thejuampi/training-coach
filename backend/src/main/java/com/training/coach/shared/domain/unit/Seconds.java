package com.training.coach.shared.domain.unit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record Seconds(int value) implements DurationValue {
    public Seconds {
        if (value < 0) {
            throw new IllegalArgumentException("Seconds must be non-negative");
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Seconds of(int value) {
        return new Seconds(value);
    }

    @Override
    public Seconds toSeconds() {
        return this;
    }

    @Override
    public Minutes toMinutes() {
        return new Minutes((int) Math.round(value / 60.0));
    }

    @Override
    public Hours toHours() {
        return new Hours(value / 3600.0);
    }

    @JsonValue
    public int value() {
        return value;
    }
}
