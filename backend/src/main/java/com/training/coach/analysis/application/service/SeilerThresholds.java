package com.training.coach.analysis.application.service;

import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.Watts;
import java.time.LocalDate;

public record SeilerThresholds(
        String athleteId,
        Watts lt1Watts,
        Watts lt2Watts,
        BeatsPerMinute lt1Bpm,
        BeatsPerMinute lt2Bpm,
        LocalDate effectiveDate,
        ThresholdMethod method,
        double confidence) {
    public SeilerThresholds {
        if (athleteId == null || athleteId.isBlank()) {
            throw new IllegalArgumentException("Athlete ID cannot be null or blank");
        }
        if (effectiveDate == null) {
            throw new IllegalArgumentException("Effective date is required");
        }
        if (method == null) {
            throw new IllegalArgumentException("Method is required");
        }
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("Confidence must be between 0.0 and 1.0");
        }
        if (lt1Watts != null && lt1Watts.value() < 0) {
            throw new IllegalArgumentException("LT1 watts must be non-negative");
        }
        if (lt2Watts != null && lt2Watts.value() < 0) {
            throw new IllegalArgumentException("LT2 watts must be non-negative");
        }
        if (lt1Bpm != null && lt1Bpm.value() < 0) {
            throw new IllegalArgumentException("LT1 bpm must be non-negative");
        }
        if (lt2Bpm != null && lt2Bpm.value() < 0) {
            throw new IllegalArgumentException("LT2 bpm must be non-negative");
        }
        if (lt1Watts != null && lt2Watts != null && lt2Watts.value() <= lt1Watts.value()) {
            throw new IllegalArgumentException("LT2 watts must be greater than LT1 watts");
        }
        if (lt1Bpm != null && lt2Bpm != null && lt2Bpm.value() <= lt1Bpm.value()) {
            throw new IllegalArgumentException("LT2 bpm must be greater than LT1 bpm");
        }
    }
}
