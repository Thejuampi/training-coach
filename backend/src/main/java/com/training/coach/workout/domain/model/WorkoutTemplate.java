package com.training.coach.workout.domain.model;

import com.training.coach.shared.domain.unit.Minutes;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a reusable workout template that can be inserted into training plans.
 * Templates support versioning, tagging, and sharing between coaches.
 */
public record WorkoutTemplate(
        String id,
        String name,
        String description,
        WorkoutType type,
        Minutes duration,
        String intensityProfile,
        TemplateStatus status,
        int version,
        String coachId,
        Set<String> tags,
        Set<String> phases,
        Set<String> purposes,
        boolean isKeySession,
        Instant createdAt,
        Instant updatedAt,
        Instant publishedAt,
        String parentTemplateId,  // For versioning
        boolean deprecated,
        boolean sharedGlobally
) {
    public WorkoutTemplate {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Template ID cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Template name cannot be null or blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("Workout type cannot be null");
        }
        if (duration == null) {
            throw new IllegalArgumentException("Duration cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Template status cannot be null");
        }
        if (coachId == null || coachId.isBlank()) {
            throw new IllegalArgumentException("Coach ID cannot be null or blank");
        }
        if (tags == null) {
            tags = Set.of();
        }
        if (phases == null) {
            phases = Set.of();
        }
        if (purposes == null) {
            purposes = Set.of();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }

    /**
     * Create a new workout template.
     */
    public static WorkoutTemplate create(
            String name,
            String description,
            WorkoutType type,
            Minutes duration,
            String intensityProfile,
            String coachId
    ) {
        return new WorkoutTemplate(
                UUID.randomUUID().toString(),
                name,
                description,
                type,
                duration,
                intensityProfile,
                TemplateStatus.DRAFT,
                1,
                coachId,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                false,
                Instant.now(),
                Instant.now(),
                null,
                null,
                false,
                false
        );
    }

    /**
     * Publish the template.
     */
    public WorkoutTemplate publish() {
        return new WorkoutTemplate(
                id,
                name,
                description,
                type,
                duration,
                intensityProfile,
                TemplateStatus.PUBLISHED,
                version,
                coachId,
                tags,
                phases,
                purposes,
                isKeySession,
                createdAt,
                Instant.now(),
                Instant.now(),
                parentTemplateId,
                deprecated,
                sharedGlobally
        );
    }

    /**
     * Deprecate the template.
     */
    public WorkoutTemplate deprecate() {
        return new WorkoutTemplate(
                id,
                name,
                description,
                type,
                duration,
                intensityProfile,
                status,
                version,
                coachId,
                tags,
                phases,
                purposes,
                isKeySession,
                createdAt,
                Instant.now(),
                publishedAt,
                parentTemplateId,
                true,
                sharedGlobally
        );
    }

    /**
     * Create a new version of this template.
     */
    public WorkoutTemplate createNewVersion(
            String name,
            String description,
            Minutes duration,
            String intensityProfile
    ) {
        return new WorkoutTemplate(
                UUID.randomUUID().toString(),
                name,
                description,
                type,
                duration,
                intensityProfile,
                TemplateStatus.DRAFT,
                version + 1,
                coachId,
                tags,
                phases,
                purposes,
                isKeySession,
                Instant.now(),
                Instant.now(),
                null,
                id,  // Link to parent template
                false,
                sharedGlobally
        );
    }

    /**
     * Add tags to the template.
     */
    public WorkoutTemplate withTags(Set<String> newTags) {
        return new WorkoutTemplate(
                id,
                name,
                description,
                type,
                duration,
                intensityProfile,
                status,
                version,
                coachId,
                new HashSet<>(newTags),
                phases,
                purposes,
                isKeySession,
                createdAt,
                Instant.now(),
                publishedAt,
                parentTemplateId,
                deprecated,
                sharedGlobally
        );
    }

    /**
     * Add phase tags.
     */
    public WorkoutTemplate withPhases(Set<String> newPhases) {
        return new WorkoutTemplate(
                id,
                name,
                description,
                type,
                duration,
                intensityProfile,
                status,
                version,
                coachId,
                tags,
                new HashSet<>(newPhases),
                purposes,
                isKeySession,
                createdAt,
                Instant.now(),
                publishedAt,
                parentTemplateId,
                deprecated,
                sharedGlobally
        );
    }

    /**
     * Add purpose tags.
     */
    public WorkoutTemplate withPurposes(Set<String> newPurposes) {
        return new WorkoutTemplate(
                id,
                name,
                description,
                type,
                duration,
                intensityProfile,
                status,
                version,
                coachId,
                tags,
                phases,
                new HashSet<>(newPurposes),
                isKeySession,
                createdAt,
                Instant.now(),
                publishedAt,
                parentTemplateId,
                deprecated,
                sharedGlobally
        );
    }

    /**
     * Mark as key session.
     */
    public WorkoutTemplate markAsKeySession() {
        return new WorkoutTemplate(
                id,
                name,
                description,
                type,
                duration,
                intensityProfile,
                status,
                version,
                coachId,
                tags,
                phases,
                purposes,
                true,
                createdAt,
                Instant.now(),
                publishedAt,
                parentTemplateId,
                deprecated,
                sharedGlobally
        );
    }

    /**
     * Enable global sharing.
     */
    public WorkoutTemplate enableSharing() {
        return new WorkoutTemplate(
                id,
                name,
                description,
                type,
                duration,
                intensityProfile,
                status,
                version,
                coachId,
                tags,
                phases,
                purposes,
                isKeySession,
                createdAt,
                Instant.now(),
                publishedAt,
                parentTemplateId,
                deprecated,
                true
        );
    }

    /**
     * Check if template is available for use.
     */
    public boolean isAvailable() {
        return status == TemplateStatus.PUBLISHED && !deprecated;
    }

    /**
     * Check if template has a specific tag.
     */
    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    /**
     * Check if template is for a specific phase.
     */
    public boolean isForPhase(String phase) {
        return phases.contains(phase);
    }

    /**
     * Check if template serves a specific purpose.
     */
    public boolean servesPurpose(String purpose) {
        return purposes.contains(purpose);
    }

    /**
     * Types of workouts.
     */
    public enum WorkoutType {
        ENDURANCE,
        INTERVALS,
        TEMPO,
        THRESHOLD,
        RECOVERY,
        RACE,
        STRENGTH,
        FLEXIBILITY,
        OTHER
    }

    /**
     * Status of a template.
     */
    public enum TemplateStatus {
        DRAFT,      // Not yet ready for use
        PUBLISHED,  // Available for use
        DEPRECATED  // No longer recommended
    }
}