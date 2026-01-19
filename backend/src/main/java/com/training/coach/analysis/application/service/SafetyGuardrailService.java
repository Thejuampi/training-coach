package com.training.coach.analysis.application.service;

import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Service for evaluating safety guardrails on training adjustments.
 * Blocks high-intensity workouts when athlete fatigue is elevated.
 */
@Service
public class SafetyGuardrailService {

    private static final int HIGH_FATIGUE_THRESHOLD = 7;
    private static final int HIGH_SORENESS_THRESHOLD = 7;
    private static final double LOW_READINESS_THRESHOLD = 4.0;

    /**
     * Check if an adjustment is blocked by safety guardrails.
     */
    public GuardrailResult checkAdjustment(
            String athleteId,
            double fatigueScore,
            double sorenessScore,
            double readinessScore,
            String workoutType) {

        boolean blocked = false;
        String blockingRule = null;
        String safeAlternative = null;

        // Block high-intensity workouts when fatigue or soreness is high
        if (isHighIntensityWorkout(workoutType)) {
            if (fatigueScore >= HIGH_FATIGUE_THRESHOLD) {
                blocked = true;
                blockingRule = "High intensity workouts are blocked when fatigue score is "
                        + HIGH_FATIGUE_THRESHOLD + " or higher (current: " + fatigueScore + ")";
                safeAlternative = "Schedule a recovery ride or easy endurance workout instead";
            } else if (sorenessScore >= HIGH_SORENESS_THRESHOLD) {
                blocked = true;
                blockingRule = "High intensity workouts are blocked when muscle soreness is "
                        + HIGH_SORENESS_THRESHOLD + " or higher (current: " + sorenessScore + ")";
                safeAlternative = "Schedule an active recovery day with easy movement";
            } else if (readinessScore <= LOW_READINESS_THRESHOLD) {
                blocked = true;
                blockingRule = "High intensity workouts are blocked when readiness score is "
                        + LOW_READINESS_THRESHOLD + " or below (current: " + readinessScore + ")";
                safeAlternative = "Schedule low-intensity recovery training";
            }
        }

        return new GuardrailResult(blocked, blockingRule, safeAlternative);
    }

    private boolean isHighIntensityWorkout(String workoutType) {
        return switch (workoutType.toUpperCase()) {
            case "INTERVALS", "VO2_MAX", "THRESHOLD", "SPRINT" -> true;
            default -> false;
        };
    }

    public record GuardrailResult(
            boolean blocked,
            String blockingRule,
            String safeAlternative
    ) {}
}
