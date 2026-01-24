package com.training.coach.trainingplan.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Represents a plan rebase operation, tracking the history of plan date changes.
 * When an event date changes, the plan may need to be rebased to the new date.
 */
public record PlanRebase(
        String id,
        String planId,
        int planVersion,
        LocalDate originalEndDate,
        LocalDate newEndDate,
        String reason,
        Instant rebasedAt,
        String performedBy,
        List<WorkoutAdjustment> workoutAdjustments
) {
    public PlanRebase {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Rebase ID cannot be null or blank");
        }
        if (planId == null || planId.isBlank()) {
            throw new IllegalArgumentException("Plan ID cannot be null or blank");
        }
        if (planVersion <= 0) {
            throw new IllegalArgumentException("Plan version must be positive");
        }
        if (originalEndDate == null || newEndDate == null) {
            throw new IllegalArgumentException("Rebase dates cannot be null");
        }
        if (rebasedAt == null) {
            throw new IllegalArgumentException("Rebase timestamp cannot be null");
        }
    }

    /**
     * Create a plan rebase record.
     */
    public static PlanRebase create(
            String planId,
            int planVersion,
            LocalDate originalEndDate,
            LocalDate newEndDate,
            String reason,
            String performedBy
    ) {
        return new PlanRebase(
                UUID.randomUUID().toString(),
                planId,
                planVersion,
                originalEndDate,
                newEndDate,
                reason,
                Instant.now(),
                performedBy,
                List.of()
        );
    }

    /**
     * Calculate the number of days the plan was shifted.
     */
    public int daysShifted() {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(originalEndDate, newEndDate);
    }

    /**
     * Check if the rebase moved the plan forward (positive shift) or backward (negative shift).
     */
    public boolean isForwardShift() {
        return newEndDate.isAfter(originalEndDate);
    }

    /**
     * Represents a single workout adjustment during the rebase.
     */
    public record WorkoutAdjustment(
            String workoutId,
            LocalDate originalDate,
            LocalDate newDate,
            String adjustmentType
    ) {
        public enum AdjustmentType {
            SHIFTED,        // Date was shifted due to rebase
            PRESERVED,      // Kept at same date (e.g., completed workout)
            REMOVED,        // Removed due to date conflict
            ADAPTED         // Modified for new schedule
        }
    }
}