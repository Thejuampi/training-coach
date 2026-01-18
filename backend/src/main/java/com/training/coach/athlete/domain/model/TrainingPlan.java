package com.training.coach.athlete.domain.model;

import com.training.coach.shared.domain.unit.Hours;
import java.time.LocalDate;
import java.util.List;

/**
 * Immutable value object representing a training plan.
 */
public record TrainingPlan(
        String id,
        String athleteId,
        LocalDate startDate,
        LocalDate endDate,
        List<Workout> workouts,
        Hours totalVolumeHours, // Computed total training hours
        String notes) {
    public TrainingPlan {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        if (workouts == null) {
            throw new IllegalArgumentException("Workouts cannot be null");
        }
        if (totalVolumeHours != null && totalVolumeHours.value() < 0) {
            throw new IllegalArgumentException("Total volume must be non-negative");
        }
    }

    /**
     * Creates a training plan with computed total volume.
     */
    public static TrainingPlan create(
            String id, String athleteId, LocalDate startDate, LocalDate endDate, List<Workout> workouts, String notes) {
        int totalMinutes =
                workouts.stream().mapToInt(w -> w.durationMinutes().value()).sum();
        Hours volume = Hours.fromMinutes(totalMinutes);
        return new TrainingPlan(id, athleteId, startDate, endDate, workouts, volume, notes);
    }
}
