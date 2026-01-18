package com.training.coach.trainingplan.domain.model;

import com.training.coach.athlete.domain.model.Workout;
import com.training.coach.trainingplan.infrastructure.persistence.entity.PlanVersionStatus;
import java.time.Instant;
import java.util.List;

/**
 * Domain model for a plan version.
 */
public record PlanVersion(
        String planId, int versionNumber, PlanVersionStatus status, List<Workout> workouts, Instant createdAt) {

    public static PlanVersion create(String planId, int versionNumber, List<Workout> workouts) {
        return new PlanVersion(planId, versionNumber, PlanVersionStatus.DRAFT, workouts, Instant.now());
    }
}
