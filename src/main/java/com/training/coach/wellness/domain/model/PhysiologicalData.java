package com.training.coach.wellness.domain.model;

import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.HeartRateVariability;
import com.training.coach.shared.domain.unit.Kilograms;

public record PhysiologicalData(
        BeatsPerMinute restingHeartRate, HeartRateVariability hrv, Kilograms bodyWeightKg, SleepMetrics sleep) {
    public PhysiologicalData {
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
}
