package com.training.coach.athlete.domain.model;

import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.HeartRateVariability;
import com.training.coach.shared.domain.unit.Kilograms;
import java.time.LocalDate;

/**
 * Immutable value object representing an athlete's readiness snapshot.
 * Combines subjective and objective data to assess training readiness.
 */
public record ReadinessSnapshot(
        LocalDate date,
        int fatigueScore, // 1-10, higher = more fatigued
        int stressScore, // 1-10, higher = more stressed
        int sleepScore, // 1-10, higher = better sleep
        int motivationScore, // 1-10, higher = more motivated
        BeatsPerMinute restingHeartRate, // bpm, optional
        HeartRateVariability hrv, // Heart Rate Variability, optional
        Kilograms bodyWeightKg, // optional
        String notes // optional subjective notes
        ) {
    public ReadinessSnapshot {
        if (fatigueScore < 1 || fatigueScore > 10) {
            throw new IllegalArgumentException("Fatigue score must be between 1 and 10");
        }
        if (stressScore < 1 || stressScore > 10) {
            throw new IllegalArgumentException("Stress score must be between 1 and 10");
        }
        if (sleepScore < 1 || sleepScore > 10) {
            throw new IllegalArgumentException("Sleep score must be between 1 and 10");
        }
        if (motivationScore < 1 || motivationScore > 10) {
            throw new IllegalArgumentException("Motivation score must be between 1 and 10");
        }
        if (restingHeartRate != null && restingHeartRate.value() < 0) {
            throw new IllegalArgumentException("Resting heart rate must be non-negative");
        }
        if (hrv != null && hrv.value() < 0) {
            throw new IllegalArgumentException("HRV must be non-negative");
        }
        if (bodyWeightKg != null && bodyWeightKg.value() < 0) {
            throw new IllegalArgumentException("Body weight must be non-negative");
        }
    }

    /**
     * Computes a readiness score based on subjective metrics.
     * Higher score = better readiness (ready for high-intensity training).
     * Formula: Simple average of motivation and inverse fatigue/stress, plus sleep bonus.
     */
    public double readinessScore() {
        double subjectiveReadiness = (motivationScore + (11 - fatigueScore) + (11 - stressScore) + sleepScore) / 4.0;
        return Math.min(10.0, subjectiveReadiness); // Cap at 10
    }
}
