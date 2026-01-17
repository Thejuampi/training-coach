package com.training.coach.trainingplan.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.training.coach.athlete.domain.model.Workout;
import com.training.coach.shared.domain.unit.Minutes;
import com.training.coach.shared.domain.unit.Percent;
import com.training.coach.trainingplan.domain.model.PlanSummary;
import com.training.coach.trainingplan.domain.model.PlanVersion;
import com.training.coach.trainingplan.infrastructure.persistence.entity.PlanVersionEntity;
import com.training.coach.trainingplan.infrastructure.persistence.entity.PlanVersionStatus;
import com.training.coach.trainingplan.infrastructure.persistence.entity.PlanWorkoutEntity;
import com.training.coach.trainingplan.infrastructure.persistence.entity.TrainingPlanEntity;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PlanRepositoryAdapterTest {

    @Test
    void saveSetsTitleAndLeavesUpdatedAtForJpa() {
        TrainingPlanJpaRepository trainingPlanRepo = mock(TrainingPlanJpaRepository.class);
        PlanVersionJpaRepository planVersionRepo = mock(PlanVersionJpaRepository.class);
        PlanWorkoutJpaRepository planWorkoutRepo = mock(PlanWorkoutJpaRepository.class);
        PlanRepositoryAdapter adapter = new PlanRepositoryAdapter(trainingPlanRepo, planVersionRepo, planWorkoutRepo);

        Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");
        PlanSummary planSummary =
                new PlanSummary("plan-1", "athlete-1", 1, PlanVersionStatus.DRAFT, createdAt);

        adapter.save(planSummary);

        ArgumentCaptor<TrainingPlanEntity> captor = ArgumentCaptor.forClass(TrainingPlanEntity.class);
        verify(trainingPlanRepo).save(captor.capture());
        TrainingPlanEntity saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("Plan plan-1");
        assertThat(saved.getCreatedAt()).isEqualTo(createdAt);
        assertThat(saved.getUpdatedAt()).isNull();
    }

    @Test
    void saveVersionUsesGeneratedIdForWorkouts() {
        TrainingPlanJpaRepository trainingPlanRepo = mock(TrainingPlanJpaRepository.class);
        PlanVersionJpaRepository planVersionRepo = mock(PlanVersionJpaRepository.class);
        PlanWorkoutJpaRepository planWorkoutRepo = mock(PlanWorkoutJpaRepository.class);
        PlanRepositoryAdapter adapter = new PlanRepositoryAdapter(trainingPlanRepo, planVersionRepo, planWorkoutRepo);

        Workout workout = new Workout(
                "workout-1",
                LocalDate.of(2024, 1, 1),
                Workout.WorkoutType.ENDURANCE,
                Minutes.of(60),
                new Workout.IntensityProfile(
                        Percent.of(100), Percent.of(0), Percent.of(0), Percent.of(0), Percent.of(0)),
                List.of());
        PlanVersion version = new PlanVersion(
                "plan-1", 1, PlanVersionStatus.DRAFT, List.of(workout), Instant.parse("2024-01-01T00:00:00Z"));

        when(planVersionRepo.save(any(PlanVersionEntity.class))).thenAnswer(invocation -> {
            PlanVersionEntity entity = invocation.getArgument(0);
            assertThat(entity.getId()).isNull();
            entity.setId("version-123");
            return entity;
        });

        adapter.saveVersion(version);

        ArgumentCaptor<PlanWorkoutEntity> workoutCaptor = ArgumentCaptor.forClass(PlanWorkoutEntity.class);
        verify(planWorkoutRepo).save(workoutCaptor.capture());
        PlanWorkoutEntity savedWorkout = workoutCaptor.getValue();
        assertThat(savedWorkout.getPlanVersionId()).isEqualTo("version-123");
        assertThat(savedWorkout.getId()).isEqualTo("workout-1");
    }
}
