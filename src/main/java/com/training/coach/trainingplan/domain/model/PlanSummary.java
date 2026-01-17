package com.training.coach.trainingplan.domain.model;

import com.training.coach.trainingplan.infrastructure.persistence.entity.PlanVersionStatus;
import java.time.Instant;

/**
 * Domain model for plan summary.
 */
public record PlanSummary(
        String id, String athleteId, int currentVersion, PlanVersionStatus status, Instant createdAt) {}
