package com.training.coach.athlete.domain.model;

/**
 * Types of notifications that can be sent to athletes and coaches.
 */
public enum NotificationType {
    // Workout related
    WORKOUT_REMINDER("Daily workout reminder"),
    MISSED_SESSION("Missed workout session"),
    KEY_SESSION_ALERT("Key session missed by athlete"),

    // Wellness related
    WELLNESS_REMINDER("Reminder to submit wellness data"),
    WELLNESS_SUBMISSION_CONFIRMATION("Wellness data submitted successfully"),

    // Readiness and fatigue
    FATIGUE_WARNING("Fatigue warning - low readiness detected"),
    RECOVERY_REMINDER("Recovery reminder"),

    // Safety and guardrails
    SAFETY_VIOLATION("Safety guardrail violation"),
    LOAD_WARNING("Training load warning"),

    // Plan and schedule
    PLAN_UPDATED("Training plan updated"),
    SCHEDULE_CHANGE("Schedule change notification"),

    // System and administrative
    SYNC_ISSUE("Data sync issue"),
    WELCOME_MESSAGE("Welcome to training platform"),

    // Conflict resolution
    CONFLICT_DETECTED("Activity conflict detected"),
    CONFLICT_RESOLUTION("Conflict resolution available");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}