package com.training.coach.analysis.application.service;

import com.training.coach.athlete.domain.model.ReadinessSnapshot;
import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.HeartRateVariability;
import com.training.coach.shared.domain.unit.Hours;
import com.training.coach.shared.domain.unit.Kilograms;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

/**
 * Application service for calculating athlete readiness.
 */
@Service
public class ReadinessService {

    public ReadinessSnapshot calculateReadiness(
            BeatsPerMinute rhr, HeartRateVariability hrv, Hours sleepHours, int sleepQuality) {
        // Map metrics to 1-10 scores
        int fatigueScore = rhr.value() > 65 ? 8 : rhr.value() > 60 ? 5 : 3; // Higher RHR = more fatigue
        int stressScore = hrv.value() < 40 ? 8 : hrv.value() < 50 ? 5 : 3; // Lower HRV = more stress
        int sleepScore = Math.min(10, (int) sleepHours.value() + (sleepQuality / 2)); // Hours + quality/2
        int motivationScore = 7; // Assume average for MVP

        return new ReadinessSnapshot(
                LocalDate.now(),
                fatigueScore,
                stressScore,
                sleepScore,
                motivationScore,
                rhr,
                hrv,
                Kilograms.of(0.0), // bodyWeightKg unknown
                "Calculated from wellness data");
    }
}
