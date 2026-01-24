package com.training.coach.reconciliation.domain.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a conflict detected between data from multiple platforms.
 * Used for tracking and resolving discrepancies in activity data.
 */
public record DataConflict(
        String id,
        String athleteId,
        LocalDateTime activityDate,
        ConflictType type,
        ConflictStatus status,
        Map<String, ConflictRecord> conflictingRecords,
        String primaryPlatform,
        Instant detectedAt,
        Instant resolvedAt,
        String resolution,
        List<String> retainedSources
) {
    public DataConflict {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Conflict ID cannot be null or blank");
        }
        if (athleteId == null || athleteId.isBlank()) {
            throw new IllegalArgumentException("Athlete ID cannot be null or blank");
        }
        if (activityDate == null) {
            throw new IllegalArgumentException("Activity date cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Conflict type cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Conflict status cannot be null");
        }
        if (conflictingRecords == null || conflictingRecords.isEmpty()) {
            throw new IllegalArgumentException("Conflicting records cannot be null or empty");
        }
        if (detectedAt == null) {
            throw new IllegalArgumentException("Detected at timestamp cannot be null");
        }
    }

    /**
     * Create a new data conflict.
     */
    public static DataConflict create(
            String athleteId,
            LocalDateTime activityDate,
            ConflictType type,
            Map<String, ConflictRecord> conflictingRecords
    ) {
        return new DataConflict(
                UUID.randomUUID().toString(),
                athleteId,
                activityDate,
                type,
                ConflictStatus.DETECTED,
                conflictingRecords,
                null,
                Instant.now(),
                null,
                null,
                List.of()
        );
    }

    /**
     * Mark the conflict as resolved.
     */
    public DataConflict resolve(String resolution, String primaryPlatform, List<String> retainedSources) {
        return new DataConflict(
                id,
                athleteId,
                activityDate,
                type,
                ConflictStatus.RESOLVED,
                conflictingRecords,
                primaryPlatform,
                detectedAt,
                Instant.now(),
                resolution,
                retainedSources
        );
    }

    /**
     * Mark the conflict as requiring manual review.
     */
    public DataConflict requireManualReview() {
        return new DataConflict(
                id,
                athleteId,
                activityDate,
                type,
                ConflictStatus.REQUIRES_REVIEW,
                conflictingRecords,
                primaryPlatform,
                detectedAt,
                null,
                null,
                List.of()
        );
    }

    /**
     * Check if the conflict is resolved.
     */
    public boolean isResolved() {
        return status == ConflictStatus.RESOLVED;
    }

    /**
     * Check if the conflict requires manual review.
     */
    public boolean requiresManualReview() {
        return status == ConflictStatus.REQUIRES_REVIEW;
    }

    /**
     * Record representing conflicting data from a single platform.
     */
    public record ConflictRecord(
            String platform,
            String activityId,
            double durationMinutes,
            LocalDateTime startTime,
            Map<String, Object> metadata
    ) {
        public ConflictRecord {
            if (platform == null || platform.isBlank()) {
                throw new IllegalArgumentException("Platform cannot be null or blank");
            }
            if (activityId == null || activityId.isBlank()) {
                throw new IllegalArgumentException("Activity ID cannot be null or blank");
            }
        }
    }

    /**
     * Types of conflicts that can occur.
     */
    public enum ConflictType {
        DUPLICATE,           // Same activity appears on multiple platforms
        OVERLAPPING,         // Activities overlap in time
        DISCREPANT_DATA,     // Same activity has different values
        MISSING_DATA,        // Expected data is missing from one platform
        TEMPORAL_MISMATCH    // Time/date discrepancies
    }

    /**
     * Status of a conflict through its lifecycle.
     */
    public enum ConflictStatus {
        DETECTED,          // Conflict has been detected
        REQUIRES_REVIEW,   // Needs manual intervention
        RESOLVED,          // Conflict has been resolved
        IGNORED            // Conflict was ignored
    }
}