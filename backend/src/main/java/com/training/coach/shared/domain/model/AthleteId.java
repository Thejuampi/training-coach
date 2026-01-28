package com.training.coach.shared.domain.model;

import java.util.Objects;

/**
 * Value object representing an athlete's unique identifier.
 */
public record AthleteId(String value) {
    public AthleteId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("AthleteId cannot be null or blank");
        }
    }

    /**
     * Create an AthleteId from a string value.
     */
    public static AthleteId of(String value) {
        return new AthleteId(value);
    }

    /**
     * Get the string value of this AthleteId.
     */
    public String id() {
        return value;
    }
}
