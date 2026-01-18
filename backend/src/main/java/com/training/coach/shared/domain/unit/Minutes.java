package com.training.coach.shared.domain.unit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record Minutes(int value) implements DurationValue {
    public Minutes {
        if (value < 0) {
            throw new IllegalArgumentException("Minutes must be non-negative");
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Minutes of(int value) {
        return new Minutes(value);
    }

    public double asHours() {
        return value / 60.0;
    }

    @Override
    public Seconds toSeconds() {
        return new Seconds(value * 60);
    }

    @Override
    public Minutes toMinutes() {
        return this;
    }

    @Override
    public Hours toHours() {
        return new Hours(value / 60.0);
    }

    @JsonValue
    public int value() {
        return value;
    }
}
