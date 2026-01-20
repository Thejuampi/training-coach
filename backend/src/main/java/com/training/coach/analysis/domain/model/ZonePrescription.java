package com.training.coach.analysis.domain.model;

import com.training.coach.athlete.domain.model.PrescriptionBand;
import java.util.List;

/**
 * Immutable record representing a workout prescription with zones.
 */
public record ZonePrescription(
        String workoutId,
        List<PrescriptionBand> bands
) {
    public ZonePrescription {
        if (workoutId == null || workoutId.isBlank()) {
            throw new IllegalArgumentException("Workout ID cannot be null or blank");
        }
        if (bands == null) {
            throw new IllegalArgumentException("Bands cannot be null");
        }
        bands = List.copyOf(bands);
    }

    public static ZonePrescription forWorkout(String workoutId, List<PrescriptionBand> bands) {
        return new ZonePrescription(workoutId, bands);
    }
}
