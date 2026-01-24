package com.training.coach.athlete.domain.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a travel exception or availability exception for an athlete.
 * Used to mark periods where the athlete cannot train as scheduled.
 */
public record TravelException(
        String id,
        String athleteId,
        LocalDate startDate,
        LocalDate endDate,
        ExceptionType type,
        String description,
        ExceptionStatus status
) {
    public TravelException {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Exception ID cannot be null or blank");
        }
        if (athleteId == null || athleteId.isBlank()) {
            throw new IllegalArgumentException("Athlete ID cannot be null or blank");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Exception dates cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        if (type == null) {
            throw new IllegalArgumentException("Exception type cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Exception status cannot be null");
        }
    }

    /**
     * Create a new travel exception.
     */
    public static TravelException create(
            String athleteId,
            LocalDate startDate,
            LocalDate endDate,
            ExceptionType type,
            String description
    ) {
        return new TravelException(
                UUID.randomUUID().toString(),
                athleteId,
                startDate,
                endDate,
                type,
                description,
                ExceptionStatus.ACTIVE
        );
    }

    /**
     * Check if a given date falls within this exception period.
     */
    public boolean coversDate(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Calculate the duration of the exception in days.
     */
    public int durationDays() {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * Mark the exception as resolved.
     */
    public TravelException resolve() {
        return new TravelException(
                id,
                athleteId,
                startDate,
                endDate,
                type,
                description,
                ExceptionStatus.RESOLVED
        );
    }

    /**
     * Cancel the exception.
     */
    public TravelException cancel() {
        return new TravelException(
                id,
                athleteId,
                startDate,
                endDate,
                type,
                description,
                ExceptionStatus.CANCELLED
        );
    }

    /**
     * Types of availability exceptions.
     */
    public enum ExceptionType {
        TRAVEL,           // Athlete is traveling
        INJURY,           // Athlete is injured
        ILLNESS,          // Athlete is sick
        WORK_COMMITMENT,  // Work-related conflict
        PERSONAL,         // Personal commitment
        RECOVERY,         // Scheduled recovery period
        OTHER             // Other type of exception
    }

    /**
     * Status of an exception.
     */
    public enum ExceptionStatus {
        ACTIVE,     // Currently affecting availability
        RESOLVED,   // Exception period has passed
        CANCELLED   // Exception was cancelled
    }
}