package com.training.coach.shared.exception;

/**
 * Base exception for all training coach application exceptions.
 */
public class TrainingCoachException extends RuntimeException {

    public TrainingCoachException(String message) {
        super(message);
    }

    public TrainingCoachException(String message, Throwable cause) {
        super(message, cause);
    }

    public TrainingCoachException(Throwable cause) {
        super(cause);
    }
}
