package com.training.coach.workout.domain.model;

import com.training.coach.shared.domain.unit.Minutes;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkoutTemplateTest {

    @Test
    void createTemplate_shouldCreateValidTemplate() {
        // When
        WorkoutTemplate template = WorkoutTemplate.create(
                "5x5min VO2",
                "High intensity intervals",
                WorkoutTemplate.WorkoutType.INTERVALS,
                Minutes.of(60),
                "Z3",
                "coach-123"
        );

        // Then
        assertThat(template.name()).isEqualTo("5x5min VO2");
        assertThat(template.type()).isEqualTo(WorkoutTemplate.WorkoutType.INTERVALS);
        assertThat(template.status()).isEqualTo(WorkoutTemplate.TemplateStatus.DRAFT);
        assertThat(template.version()).isEqualTo(1);
        assertThat(template.isAvailable()).isFalse();
    }

    @Test
    void publishTemplate_shouldChangeStatusToPublished() {
        // Given
        WorkoutTemplate template = WorkoutTemplate.create(
                "Test Template",
                "Description",
                WorkoutTemplate.WorkoutType.ENDURANCE,
                Minutes.of(90),
                "Z1",
                "coach-123"
        );

        // When
        WorkoutTemplate published = template.publish();

        // Then
        assertThat(published.status()).isEqualTo(WorkoutTemplate.TemplateStatus.PUBLISHED);
        assertThat(published.isAvailable()).isTrue();
        assertThat(published.publishedAt()).isNotNull();
    }

    @Test
    void deprecateTemplate_shouldMarkAsDeprecated() {
        // Given
        WorkoutTemplate template = WorkoutTemplate.create(
                "Test Template",
                "Description",
                WorkoutTemplate.WorkoutType.ENDURANCE,
                Minutes.of(90),
                "Z1",
                "coach-123"
        ).publish();

        // When
        WorkoutTemplate deprecated = template.deprecate();

        // Then
        assertThat(deprecated.deprecated()).isTrue();
        assertThat(deprecated.isAvailable()).isFalse();
    }

    @Test
    void withTags_shouldAddTagsToTemplate() {
        // Given
        WorkoutTemplate template = WorkoutTemplate.create(
                "Test Template",
                "Description",
                WorkoutTemplate.WorkoutType.INTERVALS,
                Minutes.of(60),
                "Z3",
                "coach-123"
        );

        // When
        WorkoutTemplate tagged = template.withTags(Set.of("VO2_OPTIMAL", "build"));

        // Then
        assertThat(tagged.tags()).contains("VO2_OPTIMAL", "build");
        assertThat(tagged.hasTag("VO2_OPTIMAL")).isTrue();
    }

    @Test
    void withPhases_shouldAddPhaseTags() {
        // Given
        WorkoutTemplate template = WorkoutTemplate.create(
                "Test Template",
                "Description",
                WorkoutTemplate.WorkoutType.INTERVALS,
                Minutes.of(60),
                "Z3",
                "coach-123"
        );

        // When
        WorkoutTemplate phased = template.withPhases(Set.of("build", "peak"));

        // Then
        assertThat(phased.phases()).contains("build", "peak");
        assertThat(phased.isForPhase("build")).isTrue();
    }

    @Test
    void withPurposes_shouldAddPurposeTags() {
        // Given
        WorkoutTemplate template = WorkoutTemplate.create(
                "Test Template",
                "Description",
                WorkoutTemplate.WorkoutType.ENDURANCE,
                Minutes.of(90),
                "Z1",
                "coach-123"
        );

        // When
        WorkoutTemplate withPurposes = template.withPurposes(Set.of("FATMAX", "AEROBIC_BASE"));

        // Then
        assertThat(withPurposes.purposes()).contains("FATMAX", "AEROBIC_BASE");
        assertThat(withPurposes.servesPurpose("FATMAX")).isTrue();
    }

    @Test
    void markAsKeySession_shouldSetKeySessionFlag() {
        // Given
        WorkoutTemplate template = WorkoutTemplate.create(
                "Test Template",
                "Description",
                WorkoutTemplate.WorkoutType.INTERVALS,
                Minutes.of(60),
                "Z3",
                "coach-123"
        );

        // When
        WorkoutTemplate keySession = template.markAsKeySession();

        // Then
        assertThat(keySession.isKeySession()).isTrue();
    }

    @Test
    void enableSharing_shouldEnableGlobalSharing() {
        // Given
        WorkoutTemplate template = WorkoutTemplate.create(
                "Test Template",
                "Description",
                WorkoutTemplate.WorkoutType.INTERVALS,
                Minutes.of(60),
                "Z3",
                "coach-123"
        ).publish();

        // When
        WorkoutTemplate shared = template.enableSharing();

        // Then
        assertThat(shared.sharedGlobally()).isTrue();
    }

    @Test
    void createNewVersion_shouldIncrementVersion() {
        // Given
        WorkoutTemplate template = WorkoutTemplate.create(
                "Original Template",
                "Description",
                WorkoutTemplate.WorkoutType.INTERVALS,
                Minutes.of(60),
                "Z3",
                "coach-123"
        );

        // When
        WorkoutTemplate newVersion = template.createNewVersion(
                "Updated Template",
                "Updated description",
                Minutes.of(65),
                "Z3"
        );

        // Then
        assertThat(newVersion.version()).isEqualTo(2);
        assertThat(newVersion.name()).isEqualTo("Updated Template");
        assertThat(newVersion.parentTemplateId()).isEqualTo(template.id());
        assertThat(newVersion.duration().value()).isEqualTo(65);
    }

    @Test
    void createTemplate_withInvalidName_shouldThrowException() {
        // When/Then
        assertThatThrownBy(() ->
            WorkoutTemplate.create(
                "   ",
                "Description",
                WorkoutTemplate.WorkoutType.ENDURANCE,
                Minutes.of(60),
                "Z1",
                "coach-123"
            )
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Template name cannot be null or blank");
    }

    @Test
    void createTemplate_withNullType_shouldThrowException() {
        // When/Then
        assertThatThrownBy(() ->
            new WorkoutTemplate(
                "id",
                "name",
                "desc",
                null,
                Minutes.of(60),
                "Z1",
                WorkoutTemplate.TemplateStatus.DRAFT,
                1,
                "coach-123",
                Set.of(),
                Set.of(),
                Set.of(),
                false,
                Instant.now(),
                Instant.now(),
                null,
                false,
                false
            )
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Workout type cannot be null");
    }
}