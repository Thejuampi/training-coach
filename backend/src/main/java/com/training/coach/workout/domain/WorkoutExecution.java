package com.training.coach.workout.domain;

import com.training.coach.shared.domain.model.AthleteId;
import com.training.coach.workout.domain.model.WorkoutTemplate.WorkoutType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Records the execution of a workout by an athlete.
 */
public record WorkoutExecution(
    String id,
    AthleteId athleteId,
    String plannedWorkoutId,
    LocalDate date,
    WorkoutType type,
    int durationMinutes,
    int intensityMinutes,
    double rpe,
    String feedback,
    SkipReason skipReason,
    LocalDateTime startTime,
    LocalDateTime endTime,
    ExecutionStatus status
) {
    public static WorkoutExecution create(
        AthleteId athleteId,
        String plannedWorkoutId,
        LocalDate date,
        WorkoutType type,
        LocalDateTime startTime
    ) {
        return new WorkoutExecution(
            UUID.randomUUID().toString(),
            athleteId,
            plannedWorkoutId,
            date,
            type,
            0,
            0,
            0.0,
            null,
            null,
            startTime,
            null,
            ExecutionStatus.PLANNED
        );
    }

    public WorkoutExecution withDuration(int durationMinutes, int intensityMinutes) {
        return new WorkoutExecution(
            id,
            athleteId,
            plannedWorkoutId,
            date,
            type,
            durationMinutes,
            intensityMinutes,
            rpe,
            feedback,
            skipReason,
            startTime,
            endTime,
            status
        );
    }

    public WorkoutExecution withRpe(double rpe) {
        return new WorkoutExecution(
            id,
            athleteId,
            plannedWorkoutId,
            date,
            type,
            durationMinutes,
            intensityMinutes,
            rpe,
            feedback,
            skipReason,
            startTime,
            endTime,
            status
        );
    }

    public WorkoutExecution withFeedback(String feedback) {
        return new WorkoutExecution(
            id,
            athleteId,
            plannedWorkoutId,
            date,
            type,
            durationMinutes,
            intensityMinutes,
            rpe,
            feedback,
            skipReason,
            startTime,
            endTime,
            status
        );
    }

    public WorkoutExecution withSkipReason(SkipReason skipReason) {
        return new WorkoutExecution(
            id,
            athleteId,
            plannedWorkoutId,
            date,
            type,
            durationMinutes,
            intensityMinutes,
            rpe,
            feedback,
            skipReason,
            startTime,
            endTime,
            ExecutionStatus.SKIPPED
        );
    }

    public WorkoutExecution withStatus(ExecutionStatus status) {
        return new WorkoutExecution(
            id,
            athleteId,
            plannedWorkoutId,
            date,
            type,
            durationMinutes,
            intensityMinutes,
            rpe,
            feedback,
            skipReason,
            startTime,
            endTime,
            status
        );
    }

    public WorkoutExecution withEndTime(LocalDateTime endTime) {
        return new WorkoutExecution(
            id,
            athleteId,
            plannedWorkoutId,
            date,
            type,
            durationMinutes,
            intensityMinutes,
            rpe,
            feedback,
            skipReason,
            startTime,
            endTime,
            status
        );
    }
}