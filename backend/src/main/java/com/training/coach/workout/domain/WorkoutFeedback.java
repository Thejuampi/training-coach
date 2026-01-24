package com.training.coach.workout.domain;

import java.time.LocalDateTime;

/**
 * Represents post-workout feedback provided by an athlete.
 */
public record WorkoutFeedback(
    String id,
    String workoutExecutionId,
    double rpe,
    Integer perceivedExertion,
    String notes,
    LocalDateTime timestamp
) {
    public static WorkoutFeedback create(
        String workoutExecutionId,
        double rpe,
        Integer perceivedExertion,
        String notes
    ) {
        return new WorkoutFeedback(
            java.util.UUID.randomUUID().toString(),
            workoutExecutionId,
            rpe,
            perceivedExertion,
            notes,
            LocalDateTime.now()
        );
    }
}