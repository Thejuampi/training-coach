package com.training.coach.workout.application.service;

import com.training.coach.activity.application.port.out.ActivityRepository;
import com.training.coach.athlete.application.port.out.AthleteRepository;
import com.training.coach.athlete.domain.model.Athlete;
import com.training.coach.shared.domain.model.AthleteId;
import com.training.coach.shared.domain.model.WorkoutType;
import com.training.coach.workout.application.port.out.WorkoutExecutionRepository;
import com.training.coach.workout.application.port.out.WorkoutRepository;
import com.training.coach.workout.domain.ExecutionStatus;
import com.training.coach.workout.domain.SkipReason;
import com.training.coach.workout.domain.WorkoutExecution;
import com.training.coach.workout.domain.WorkoutFeedback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutExecutionServiceTest {

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private WorkoutExecutionRepository workoutExecutionRepository;

    @Mock
    private ActivityRepository activityRepository;

    private WorkoutExecutionService workoutExecutionService;

    @BeforeEach
    void setUp() {
        workoutExecutionService = new WorkoutExecutionService(
            athleteRepository,
            workoutRepository,
            workoutExecutionRepository,
            activityRepository
        );
    }

    @Test
    void startWorkout_shouldCreateWorkoutExecution() {
        // Given
        String athleteId = "athlete-123";
        String plannedWorkoutId = "workout-456";
        LocalDateTime startTime = LocalDateTime.now();

        Athlete athlete = new Athlete(
            "123",
            "Test Athlete",
            null,
            null,
            null,
            null
        );

        when(athleteRepository.findById(new AthleteId(athleteId))).thenReturn(Optional.of(athlete));
        when(workoutExecutionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        WorkoutExecution result = workoutExecutionService.startWorkout(athleteId, plannedWorkoutId, startTime);

        // Then
        assertThat(result.athleteId().value()).isEqualTo(athleteId);
        assertThat(result.plannedWorkoutId()).isEqualTo(plannedWorkoutId);
        assertThat(result.status()).isEqualTo(ExecutionStatus.IN_PROGRESS);
        assertThat(result.startTime()).isEqualTo(startTime);
    }

    @Test
    void completeWorkout_shouldUpdateWorkoutExecution() {
        // Given
        String executionId = "execution-123";
        int durationMinutes = 60;
        int intensityMinutes = 45;
        double rpe = 6.5;
        String feedback = "Felt good";

        WorkoutExecution existing = new WorkoutExecution(
            executionId,
            new AthleteId("athlete-123"),
            "workout-456",
            LocalDate.now(),
            WorkoutType.ENDURANCE,
            0,
            0,
            0.0,
            null,
            null,
            LocalDateTime.now(),
            null,
            ExecutionStatus.IN_PROGRESS
        );

        when(workoutExecutionRepository.findById(executionId)).thenReturn(Optional.of(existing));
        when(workoutExecutionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        WorkoutExecution result = workoutExecutionService.completeWorkout(executionId, durationMinutes, intensityMinutes, rpe, feedback);

        // Then
        assertThat(result.durationMinutes()).isEqualTo(durationMinutes);
        assertThat(result.intensityMinutes()).isEqualTo(intensityMinutes);
        assertThat(result.rpe()).isEqualTo(rpe);
        assertThat(result.feedback()).isEqualTo(feedback);
        assertThat(result.status()).isEqualTo(ExecutionStatus.COMPLETED);
    }

    @Test
    void skipWorkout_shouldMarkAsSkipped() {
        // Given
        String executionId = "execution-123";
        SkipReason skipReason = SkipReason.FATIGUE;
        String notes = "Too tired today";

        WorkoutExecution existing = new WorkoutExecution(
            executionId,
            new AthleteId("athlete-123"),
            "workout-456",
            LocalDate.now(),
            WorkoutType.ENDURANCE,
            0,
            0,
            0.0,
            null,
            null,
            LocalDateTime.now(),
            null,
            ExecutionStatus.IN_PROGRESS
        );

        when(workoutExecutionRepository.findById(executionId)).thenReturn(Optional.of(existing));
        when(workoutExecutionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        WorkoutExecution result = workoutExecutionService.skipWorkout(executionId, skipReason, notes);

        // Then
        assertThat(result.status()).isEqualTo(ExecutionStatus.SKIPPED);
        assertThat(result.skipReason()).isEqualTo(skipReason);
        assertThat(result.feedback()).contains("Skipped: " + notes);
    }

    @Test
    void calculateWorkoutQuality_shouldCalculateQualityScore() {
        // Given
        WorkoutExecution workout = new WorkoutExecution(
            "execution-123",
            new AthleteId("athlete-123"),
            "workout-456",
            LocalDate.now(),
            WorkoutType.ENDURANCE,
            85, // 94% of expected 90 minutes
            80,
            7.0, // On target
            null,
            null,
            LocalDateTime.now(),
            LocalDateTime.now().plusMinutes(85),
            ExecutionStatus.COMPLETED
        );

        // When
        double quality = workoutExecutionService.calculateWorkoutQuality(workout);

        // Then
        assertThat(quality).isGreaterThan(90.0);
        assertThat(quality).isLessThanOrEqualTo(100.0);
    }

    @Test
    void calculateWorkoutQuality_shouldPenalizeLowDuration() {
        // Given
        WorkoutExecution workout = new WorkoutExecution(
            "execution-123",
            new AthleteId("athlete-123"),
            "workout-456",
            LocalDate.now(),
            WorkoutType.ENDURANCE,
            45, // 50% of expected 90 minutes
            40,
            7.0,
            null,
            null,
            LocalDateTime.now(),
            LocalDateTime.now().plusMinutes(45),
            ExecutionStatus.COMPLETED
        );

        // When
        double quality = workoutExecutionService.calculateWorkoutQuality(workout);

        // Then
        assertThat(quality).isLessThan(70.0); // Should be significantly penalized
    }

    @Test
    void saveWorkoutFeedback_shouldCreateFeedback() {
        // Given
        String executionId = "execution-123";
        double rpe = 6.5;
        Integer perceivedExertion = 7;
        String notes = "Felt strong";

        when(workoutExecutionRepository.saveFeedback(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        WorkoutFeedback result = workoutExecutionService.saveWorkoutFeedback(executionId, rpe, perceivedExertion, notes);

        // Then
        assertThat(result.workoutExecutionId()).isEqualTo(executionId);
        assertThat(result.rpe()).isEqualTo(rpe);
        assertThat(result.perceivedExertion()).isEqualTo(perceivedExertion);
        assertThat(result.notes()).isEqualTo(notes);
    }

    @Test
    void getPendingWorkouts_shouldReturnWorkoutsWithPlannedStatus() {
        // Given
        AthleteId athleteId = new AthleteId("athlete-123");
        List<WorkoutExecution> plannedWorkouts = List.of(
            new WorkoutExecution(
                "execution-1",
                athleteId,
                "workout-1",
                LocalDate.now(),
                WorkoutType.ENDURANCE,
                0,
                0,
                0.0,
                null,
                null,
                null,
                null,
                ExecutionStatus.PLANNED
            ),
            new WorkoutExecution(
                "execution-2",
                athleteId,
                "workout-2",
                LocalDate.now().plusDays(1),
                WorkoutType.STRENGTH,
                0,
                0,
                0.0,
                null,
                null,
                null,
                null,
                ExecutionStatus.PLANNED
            )
        );

        when(workoutExecutionRepository.findByAthleteIdAndStatus(athleteId, ExecutionStatus.PLANNED))
            .thenReturn(plannedWorkouts);

        // When
        List<WorkoutExecution> result = workoutExecutionService.getPendingWorkouts("athlete-123");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(w -> w.status() == ExecutionStatus.PLANNED);
    }
}