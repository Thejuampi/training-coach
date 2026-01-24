package com.training.coach.workout.domain;

/**
 * Enumeration of workout execution statuses.
 */
public enum ExecutionStatus {
    PLANNED("Workout is planned but not yet executed"),
    IN_PROGRESS("Workout is in progress"),
    COMPLETED("Workout was completed successfully"),
    SKIPPED("Workout was skipped"),
    PARTIAL("Workout was partially completed"),
    FAILED("Workout attempt failed");

    private final String description;

    ExecutionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}