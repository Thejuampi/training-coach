package com.training.coach.trainingplan.domain.model;

import com.training.coach.trainingplan.infrastructure.persistence.entity.PlanVersionStatus;

/**
 * Summary of a training plan for listing.
 */
public record TrainingPlanSummary(
        String id, String athleteId, String title, int latestVersion, PlanVersionStatus status) {}
