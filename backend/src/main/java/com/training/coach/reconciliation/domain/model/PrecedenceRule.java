package com.training.coach.reconciliation.domain.model;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Defines precedence rules for resolving conflicts between platforms.
 * Specifies which platform's data should be retained as the source of truth.
 */
public record PrecedenceRule(
        String id,
        String athleteId,
        String ruleName,
        Map<String, Integer> platformPrecedence,
        Set<String> autoMergeFields,
        Set<String> requireManualReview,
        boolean isActive
) {
    public PrecedenceRule {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Rule ID cannot be null or blank");
        }
        if (athleteId == null || athleteId.isBlank()) {
            throw new IllegalArgumentException("Athlete ID cannot be null or blank");
        }
        if (ruleName == null || ruleName.isBlank()) {
            throw new IllegalArgumentException("Rule name cannot be null or blank");
        }
        if (platformPrecedence == null || platformPrecedence.isEmpty()) {
            throw new IllegalArgumentException("Platform precedence cannot be null or empty");
        }
        if (autoMergeFields == null) {
            autoMergeFields = Set.of();
        }
        if (requireManualReview == null) {
            requireManualReview = Set.of();
        }
    }

    /**
     * Create a new precedence rule.
     */
    public static PrecedenceRule create(
            String athleteId,
            String ruleName,
            Map<String, Integer> platformPrecedence
    ) {
        return new PrecedenceRule(
                UUID.randomUUID().toString(),
                athleteId,
                ruleName,
                platformPrecedence,
                Set.of("duration", "distance", "elevation"),
                Set.of("heartRate", "power"),
                true
        );
    }

    /**
     * Get the platform with highest precedence.
     */
    public String getPrimaryPlatform() {
        return platformPrecedence.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Get precedence value for a specific platform.
     */
    public int getPrecedence(String platform) {
        return platformPrecedence.getOrDefault(platform, 0);
    }

    /**
     * Check if a field can be auto-merged.
     */
    public boolean canAutoMerge(String field) {
        return autoMergeFields.contains(field);
    }

    /**
     * Check if a field requires manual review.
     */
    public boolean requiresManualReview(String field) {
        return requireManualReview.contains(field);
    }

    /**
     * Compare two platforms and return which has higher precedence.
     * Returns positive if platform1 has higher precedence, negative if platform2 does.
     */
    public int comparePlatforms(String platform1, String platform2) {
        return Integer.compare(
                getPrecedence(platform1),
                getPrecedence(platform2)
        );
    }

    /**
     * Update the rule.
     */
    public PrecedenceRule update(
            String ruleName,
            Map<String, Integer> platformPrecedence
    ) {
        return new PrecedenceRule(
                id,
                athleteId,
                ruleName,
                platformPrecedence,
                autoMergeFields,
                requireManualReview,
                isActive
        );
    }

    /**
     * Deactivate the rule.
     */
    public PrecedenceRule deactivate() {
        return new PrecedenceRule(
                id,
                athleteId,
                ruleName,
                platformPrecedence,
                autoMergeFields,
                requireManualReview,
                false
        );
    }

    /**
     * Common platform names.
     */
    public static class Platform {
        public static final String INTERVALS_ICU = "intervals.icu";
        public static final String STRAVA = "strava";
        public static final String GARMIN = "garmin";
        public static final String TRAINING_PEAKS = "trainingpeaks";
        public static final String WHOOP = "whoop";
    }
}
