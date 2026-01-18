package com.training.coach.shared.exception;

/**
 * Exception thrown when an athlete is not found in the system.
 */
public class AthleteNotFoundException extends TrainingCoachException {

    private final String athleteId;

    public AthleteNotFoundException(String athleteId) {
        super("Athlete not found with id: " + athleteId);
        this.athleteId = athleteId;
    }

    public String athleteId() {
        return athleteId;
    }
}
