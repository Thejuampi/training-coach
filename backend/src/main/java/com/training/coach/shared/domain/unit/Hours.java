package com.training.coach.shared.domain.unit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record Hours(double value) implements DurationValue {
    public Hours {
        if (value < 0) {
            throw new IllegalArgumentException("Hours must be non-negative");
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Hours of(double value) {
        return new Hours(value);
    }

    public static Hours fromMinutes(int minutes) {
        return new Hours(minutes / 60.0);
    }

    public Hours times(double factor) {
        return new Hours(value * factor);
    }

    public Minutes toMinutesRounded() {
        return new Minutes((int) Math.round(value * 60));
    }

    @Override
    public Seconds toSeconds() {
        return new Seconds((int) Math.round(value * 3600.0));
    }

    @Override
    public Minutes toMinutes() {
        return new Minutes((int) Math.round(value * 60.0));
    }

    @Override
    public Hours toHours() {
        return this;
    }

    @JsonValue
    public double value() {
        return value;
    }
}
