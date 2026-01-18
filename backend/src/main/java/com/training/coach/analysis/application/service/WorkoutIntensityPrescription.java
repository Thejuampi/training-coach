package com.training.coach.analysis.application.service;

public record WorkoutIntensityPrescription(
        WorkoutIntensityPurpose purpose,
        double percentFtpLower,
        double percentFtpUpper,
        PrescriptionMethod method,
        double confidence,
        String rationale) {
    public WorkoutIntensityPrescription {
        if (method == null) {
            throw new IllegalArgumentException("Method is required");
        }
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("Confidence must be between 0.0 and 1.0");
        }
    }
}
