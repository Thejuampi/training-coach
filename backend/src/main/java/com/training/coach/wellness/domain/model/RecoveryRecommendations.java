package com.training.coach.wellness.domain.model;

import java.util.List;
import java.util.Objects;

public record RecoveryRecommendations(
        String athleteId,
        List<HardRule> hardRules,
        String aiRecommendations,
        List<String> safeAdjustments,
        double readinessScore,
        double complianceRate) {
    public RecoveryRecommendations {
        if (athleteId == null || athleteId.isBlank()) {
            throw new IllegalArgumentException("Athlete ID cannot be null or blank");
        }
        if (hardRules == null) hardRules = List.of();
        if (safeAdjustments == null) safeAdjustments = List.of();
    }

    public record HardRule(String ruleName, String condition, String recommendation, boolean isBlocking) {
        public HardRule {
            if (ruleName == null || ruleName.isBlank()) {
                throw new IllegalArgumentException("Rule name cannot be null or blank");
            }
            if (recommendation == null || recommendation.isBlank()) {
                throw new IllegalArgumentException("Recommendation cannot be null or blank");
            }
        }
    }

    public static RecoveryRecommendations empty(String athleteId) {
        return new RecoveryRecommendations(athleteId, List.of(), "", List.of(), 0.0, 0.0);
    }

    public boolean hasBlockingRules() {
        return hardRules.stream().anyMatch(HardRule::isBlocking);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecoveryRecommendations that = (RecoveryRecommendations) o;
        return Objects.equals(athleteId, that.athleteId) && Objects.equals(readinessScore, that.readinessScore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(athleteId, readinessScore);
    }
}
