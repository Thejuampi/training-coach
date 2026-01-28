package com.training.coach.athlete.domain.model;

import java.time.LocalDate;

/**
 * Simple readiness value object for tracking athlete readiness over time.
 * Used for fatigue detection and notification scheduling.
 */
public record AthleteReadiness(
        String athleteId,
        double score,
        LocalDate date
) {
    public AthleteReadiness {
        if (athleteId == null || athleteId.isBlank()) {
            throw new IllegalArgumentException("Athlete ID cannot be null or blank");
        }
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Readiness score must be between 0 and 100");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
    }

    /**
     * Get the readiness score.
     * Higher score = better readiness (ready for high-intensity training).
     */
    public double score() {
        return score;
    }
}
