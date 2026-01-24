package com.training.coach.athlete.domain.model;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents an athlete's weekly availability template.
 * Defines which days of the week the athlete is available for training.
 */
public record AvailabilityTemplate(
        String id,
        String athleteId,
        Set<DayOfWeek> availableDays,
        com.training.coach.shared.domain.unit.Hours weeklyTargetHours,
        String name,
        boolean isActive
) {
    public AvailabilityTemplate {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Template ID cannot be null or blank");
        }
        if (athleteId == null || athleteId.isBlank()) {
            throw new IllegalArgumentException("Athlete ID cannot be null or blank");
        }
        if (availableDays == null || availableDays.isEmpty()) {
            throw new IllegalArgumentException("Available days cannot be null or empty");
        }
        if (weeklyTargetHours == null) {
            throw new IllegalArgumentException("Weekly target hours cannot be null");
        }
        if (weeklyTargetHours.value() <= 0) {
            throw new IllegalArgumentException("Weekly target hours must be positive");
        }
    }

    /**
     * Create a new availability template.
     */
    public static AvailabilityTemplate create(
            String athleteId,
            Set<DayOfWeek> availableDays,
            com.training.coach.shared.domain.unit.Hours weeklyTargetHours,
            String name
    ) {
        return new AvailabilityTemplate(
                UUID.randomUUID().toString(),
                athleteId,
                new HashSet<>(availableDays),
                weeklyTargetHours,
                name,
                true
        );
    }

    /**
     * Update the availability template with new days and hours.
     */
    public AvailabilityTemplate update(
            Set<DayOfWeek> newAvailableDays,
            com.training.coach.shared.domain.unit.Hours newWeeklyHours
    ) {
        return new AvailabilityTemplate(
                id,
                athleteId,
                new HashSet<>(newAvailableDays),
                newWeeklyHours,
                name,
                isActive
        );
    }

    /**
     * Update the name of the template.
     */
    public AvailabilityTemplate withName(String newName) {
        return new AvailabilityTemplate(
                id,
                athleteId,
                availableDays,
                weeklyTargetHours,
                newName,
                isActive
        );
    }

    /**
     * Set as active template.
     */
    public AvailabilityTemplate activate() {
        return new AvailabilityTemplate(
                id,
                athleteId,
                availableDays,
                weeklyTargetHours,
                name,
                true
        );
    }

    /**
     * Deactivate the template.
     */
    public AvailabilityTemplate deactivate() {
        return new AvailabilityTemplate(
                id,
                athleteId,
                availableDays,
                weeklyTargetHours,
                name,
                false
        );
    }

    /**
     * Check if a specific day is available.
     */
    public boolean isAvailable(DayOfWeek day) {
        return availableDays.contains(day);
    }

    /**
     * Get the number of available days per week.
     */
    public int availableDaysCount() {
        return availableDays.size();
    }

    /**
     * Calculate average training hours per available day.
     */
    public double averageHoursPerDay() {
        return weeklyTargetHours.value() / availableDays.size();
    }
}