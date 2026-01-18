package com.training.coach.athlete.domain.model;

import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.Kilograms;
import com.training.coach.shared.domain.unit.Vo2Max;
import com.training.coach.shared.domain.unit.Watts;

/**
 * Immutable value object representing athlete's training metrics.
 */
public record TrainingMetrics(
        Watts ftp, // Functional Threshold Power (watts)
        BeatsPerMinute fthr, // Functional Threshold Heart Rate (bpm)
        Vo2Max vo2max, // VO2 Max (ml/kg/min)
        Kilograms weightKg // Weight in kg (for calculations)
        ) {}
