package com.training.coach.athlete.domain.model;

import java.time.LocalDate;

/**
 * Represents a goal event (race, competition, or important training milestone).
 */
public record Event(
        String id,
        String athleteId,
        String name,
        LocalDate date,
        EventPriority priority,
        EventType type,
        String notes
) {
    public Event {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Event ID cannot be null or blank");
        }
        if (athleteId == null || athleteId.isBlank()) {
            throw new IllegalArgumentException("Athlete ID cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Event name cannot be null or blank");
        }
        if (date == null) {
            throw new IllegalArgumentException("Event date cannot be null");
        }
        if (priority == null) {
            throw new IllegalArgumentException("Event priority cannot be null");
        }
    }

    /**
     * Create an event with minimal required fields.
     */
    public static Event create(String athleteId, String name, LocalDate date, EventPriority priority) {
        return new Event(
                java.util.UUID.randomUUID().toString(),
                athleteId,
                name,
                date,
                priority,
                EventType.RACE,
                null
        );
    }

    /**
     * Update the event date, used when rebasing plans.
     */
    public Event withDate(LocalDate newDate) {
        return new Event(id, athleteId, name, newDate, priority, type, notes);
    }

    /**
     * Check if this is an A-priority event requiring taper.
     */
    public boolean requiresTaper() {
        return priority == EventPriority.A;
    }

    /**
     * Calculate taper duration based on event priority.
     */
    public int taperDays() {
        return switch (priority) {
            case A -> 14;  // 2 weeks for A races
            case B -> 7;   // 1 week for B races
            case C -> 3;   // 3 days for C races
        };
    }

    /**
     * Priority levels for events.
     */
    public enum EventPriority {
        A,  // Key event - requires full taper and peak
        B,  // Important event - moderate taper
        C   // Training event - minimal or no taper
    }

    /**
     * Types of events.
     */
    public enum EventType {
        RACE,           // Official competition
        TRAINING,       // Important training milestone
        TIME_TRIAL,     // Performance test
        FUN_EVENT,      // Casual participation event
        OTHER           // Other type of event
    }
}