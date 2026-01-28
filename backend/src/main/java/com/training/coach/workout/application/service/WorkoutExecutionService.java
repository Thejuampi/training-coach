package com.training.coach.workout.application.service;

import com.training.coach.activity.application.port.out.ActivityRepository;
import com.training.coach.athlete.application.port.out.AthleteRepository;
import com.training.coach.athlete.domain.model.Athlete;
import com.training.coach.shared.domain.model.AthleteId;
import com.training.coach.workout.domain.model.WorkoutTemplate.WorkoutType;
import com.training.coach.workout.application.port.out.WorkoutExecutionRepository;
import com.training.coach.workout.application.port.out.WorkoutRepository;
import com.training.coach.workout.domain.ExecutionStatus;
import com.training.coach.workout.domain.SkipReason;
import com.training.coach.workout.domain.WorkoutExecution;
import com.training.coach.workout.domain.WorkoutFeedback;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for managing workout execution, matching, and feedback.
 */
@Service
public class WorkoutExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutExecutionService.class);

    private final AthleteRepository athleteRepository;
    private final WorkoutRepository workoutRepository;
    private final WorkoutExecutionRepository workoutExecutionRepository;
    private final ActivityRepository activityRepository;

    public WorkoutExecutionService(
            AthleteRepository athleteRepository,
            WorkoutRepository workoutRepository,
            WorkoutExecutionRepository workoutExecutionRepository,
            ActivityRepository activityRepository) {
        this.athleteRepository = athleteRepository;
        this.workoutRepository = workoutRepository;
        this.workoutExecutionRepository = workoutExecutionRepository;
        this.activityRepository = activityRepository;
    }

    /**
     * Start a workout execution.
     */
    public WorkoutExecution startWorkout(String athleteId, String plannedWorkoutId, LocalDateTime startTime) {
        // Verify athlete exists
        var athleteResult = athleteRepository.findById(athleteId);
        if (!athleteResult.isPresent()) {
            throw new IllegalArgumentException("Athlete not found: " + athleteId);
        }

        // Create workout execution record
        var workoutExecution = WorkoutExecution.create(
            new AthleteId(athleteId),
            plannedWorkoutId,
            startTime.toLocalDate(),
            WorkoutType.ENDURANCE, // Will be determined from planned workout
            startTime
        );

        workoutExecution = workoutExecution.withStatus(ExecutionStatus.IN_PROGRESS);
        return workoutExecutionRepository.save(workoutExecution);
    }

    /**
     * Complete a workout with duration and intensity data.
     */
    public WorkoutExecution completeWorkout(
            String executionId,
            int durationMinutes,
            int intensityMinutes,
            double rpe,
            String feedback
    ) {
        var existingExecution = workoutExecutionRepository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Workout execution not found: " + executionId));

        var completed = existingExecution
                .withDuration(durationMinutes, intensityMinutes)
                .withRpe(rpe)
                .withStatus(ExecutionStatus.COMPLETED);

        if (feedback != null && !feedback.trim().isEmpty()) {
            completed = completed.withFeedback(feedback);
        }

        return workoutExecutionRepository.save(completed);
    }

    /**
     * Mark a workout as skipped with a reason.
     */
    public WorkoutExecution skipWorkout(String executionId, SkipReason skipReason, String notes) {
        var existingExecution = workoutExecutionRepository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Workout execution not found: " + executionId));

        var skipped = existingExecution.withSkipReason(skipReason);

        if (notes != null && !notes.trim().isEmpty()) {
            skipped = skipped.withFeedback("Skipped: " + notes);
        }

        return workoutExecutionRepository.save(skipped);
    }

    /**
     * Match a completed activity to a planned workout.
     */
    public WorkoutExecution matchActivityToWorkout(String athleteId, String activityId) {
        // Get the activity - find by athlete ID and activity external ID
        var activityOpt = activityRepository.findByAthleteIdAndExternalActivityId(athleteId, activityId);
        var activity = activityOpt
                .orElseThrow(() -> new IllegalArgumentException("Activity not found: " + activityId));

        // Find planned workout for the same date
        var plannedWorkout = workoutRepository.findByAthleteIdAndDate(
            new AthleteId(athleteId), activity.date()
        ).orElseThrow(() -> new IllegalArgumentException("No planned workout found for date: " + activity.date()));

        // Determine workout type based on activity type
        var workoutType = determineWorkoutType(activity.type());

        // Create or update workout execution - using date range query
        var startOfDay = activity.date().atStartOfDay();
        var endOfDay = activity.date().atTime(23, 59, 59);
        var executions = workoutExecutionRepository.findByAthleteIdAndDateRange(
            new AthleteId(athleteId), startOfDay, endOfDay
        );
        var existingExecution = executions.isEmpty() ? null : executions.get(0);

        WorkoutExecution execution;
        int durationMinutes = (int) (activity.durationSeconds().value() / 60);
        if (existingExecution != null) {
            execution = existingExecution
                    .withDuration(durationMinutes, 0)
                    .withStatus(ExecutionStatus.COMPLETED);
        } else {
            execution = WorkoutExecution.create(
                new AthleteId(athleteId),
                plannedWorkout.id(),
                activity.date(),
                workoutType,
                LocalDateTime.now()
            ).withDuration(durationMinutes, 0)
             .withStatus(ExecutionStatus.COMPLETED);
        }

        return workoutExecutionRepository.save(execution);
    }

    /**
     * Determine workout type based on activity type.
     */
    private WorkoutType determineWorkoutType(String activityType) {
        switch (activityType.toUpperCase()) {
            case "RIDE":
            case "RUN":
                return WorkoutType.ENDURANCE;
            case "HIKE":
            case "WALK":
                return WorkoutType.RECOVERY;
            case "STRENGTH":
            case "WEIGHTS":
                return WorkoutType.STRENGTH;
            case "YOGA":
            case "STRETCH":
                return WorkoutType.FLEXIBILITY;
            case "RACING":
                return WorkoutType.RACE;
            default:
                return WorkoutType.ENDURANCE;
        }
    }

    /**
     * Get workout executions for an athlete by date range.
     */
    public List<WorkoutExecution> getWorkoutExecutionsByDateRange(String athleteId, LocalDateTime start, LocalDateTime end) {
        var athleteIdObj = new AthleteId(athleteId);
        return workoutExecutionRepository.findByAthleteIdAndDateRange(athleteIdObj, start, end);
    }

    /**
     * Get pending workouts for an athlete.
     */
    public List<WorkoutExecution> getPendingWorkouts(String athleteId) {
        var athleteIdObj = new AthleteId(athleteId);
        return workoutExecutionRepository.findByAthleteIdAndStatus(athleteIdObj, ExecutionStatus.PLANNED);
    }

    /**
     * Save workout feedback.
     */
    public WorkoutFeedback saveWorkoutFeedback(
            String workoutExecutionId,
            double rpe,
            Integer perceivedExertion,
            String notes
    ) {
        var feedback = WorkoutFeedback.create(workoutExecutionId, rpe, perceivedExertion, notes);
        return workoutExecutionRepository.saveFeedback(feedback);
    }

    /**
     * Get the workout execution repository.
     */
    public WorkoutExecutionRepository getWorkoutExecutionRepository() {
        return workoutExecutionRepository;
    }

    /**
     * Calculate workout quality score based on duration, intensity, and RPE.
     */
    public double calculateWorkoutQuality(WorkoutExecution execution) {
        if (execution.durationMinutes() <= 0 || execution.rpe() <= 0) {
            return 0.0;
        }

        // Expected duration based on workout type
        var expectedDuration = getExpectedDuration(execution.type());
        var durationRatio = (double) execution.durationMinutes() / expectedDuration;

        // Calculate quality score (0-100)
        var quality = 100.0;

        // Penalize if duration is less than expected
        if (durationRatio < 0.8) {
            quality *= durationRatio;
        }

        // Penalize if duration exceeds expected by more than 25%
        if (durationRatio > 1.25) {
            quality *= (2.0 - durationRatio);
        }

        // Penalize for high RPE (harder than expected)
        if (execution.rpe() > 7.0) {
            quality *= (8.0 - execution.rpe()) / 8.0;
        }

        // Boost for positive feedback
        if (execution.feedback() != null && execution.feedback().toLowerCase().contains("good")) {
            quality *= 1.05;
        }

        return Math.min(100.0, Math.max(0.0, quality));
    }

    private int getExpectedDuration(WorkoutType type) {
        switch (type) {
            case ENDURANCE:
                return 90;
            case STRENGTH:
                return 60;
            case RECOVERY:
                return 45;
            case FLEXIBILITY:
                return 30;
            case RACE:
                return 120;
            default:
                return 60;
        }
    }
}