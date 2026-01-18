package com.training.coach.wellness.application.service;

import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.HeartRateVariability;
import com.training.coach.wellness.domain.model.PhysiologicalData;
import com.training.coach.wellness.domain.model.SubjectiveWellness;
import com.training.coach.wellness.domain.model.TrainingLoadSummary;
import org.springframework.stereotype.Service;

@Service
public class ReadinessCalculatorService {

    private static final double MAX_READINESS = 100.0;
    private static final double MIN_READINESS = 0.0;

    private static final double HRV_WEIGHT = 0.25;
    private static final double RHR_WEIGHT = 0.20;
    private static final double SLEEP_WEIGHT = 0.20;
    private static final double SUBJECTIVE_WEIGHT = 0.25;
    private static final double TSB_WEIGHT = 0.10;

    private static final double TYPICAL_HRV = 50.0;
    private static final double TYPICAL_RHR = 60.0;
    private static final double OPTIMAL_SLEEP_HOURS = 8.0;
    private static final double OPTIMAL_TSB = 15.0;

    public double calculateReadiness(
            PhysiologicalData physiological, SubjectiveWellness subjective, TrainingLoadSummary load) {
        double hrvScore = calculateHrvScore(physiological.hrv());
        double rhrScore = calculateRhrScore(physiological.restingHeartRate());
        double sleepScore = calculateSleepScore(physiological.sleep());
        double subjectiveScore = calculateSubjectiveScore(subjective);
        double tsbScore = load != null ? calculateTsbScore(load.tsb()) : 50.0;

        double readiness = (hrvScore * HRV_WEIGHT
                + rhrScore * RHR_WEIGHT
                + sleepScore * SLEEP_WEIGHT
                + subjectiveScore * SUBJECTIVE_WEIGHT
                + tsbScore * TSB_WEIGHT);

        return clampReadiness(readiness);
    }

    private double calculateHrvScore(HeartRateVariability hrv) {
        if (hrv == null || hrv.value() <= 0) return 50.0;
        double normalized = (hrv.value() / TYPICAL_HRV) * 75.0;
        return clampScore(normalized);
    }

    private double calculateRhrScore(BeatsPerMinute rhr) {
        if (rhr == null || rhr.value() <= 0) return 50.0;
        double ratio = TYPICAL_RHR / rhr.value();
        double normalized = ratio * 75.0;
        return clampScore(normalized);
    }

    private double calculateSleepScore(com.training.coach.wellness.domain.model.SleepMetrics sleep) {
        if (sleep == null) return 50.0;
        double hoursScore = Math.min(100.0, (sleep.totalSleepHours().value() / OPTIMAL_SLEEP_HOURS) * 80.0);
        double qualityScore = (sleep.qualityScore() - 1) / 9.0 * 20.0;
        return hoursScore + qualityScore;
    }

    private double calculateSubjectiveScore(SubjectiveWellness subjective) {
        if (subjective == null) return 50.0;
        double inverseFatigue = (11.0 - subjective.fatigueScore()) / 10.0 * 30.0;
        double inverseStress = (11.0 - subjective.stressScore()) / 10.0 * 25.0;
        double motivation = (subjective.motivationScore() - 1) / 9.0 * 25.0;
        double sorenessPenalty = (10.0 - subjective.muscleSorenessScore()) / 10.0 * 20.0;
        return inverseFatigue + inverseStress + motivation + sorenessPenalty;
    }

    private double calculateTsbScore(double tsb) {
        if (tsb <= 0) return 50.0;
        double normalized = Math.min(100.0, (tsb / OPTIMAL_TSB) * 100.0);
        return clampScore(normalized);
    }

    private double clampReadiness(double value) {
        return Math.max(MIN_READINESS, Math.min(MAX_READINESS, value));
    }

    private double clampScore(double value) {
        return Math.max(0.0, Math.min(100.0, value));
    }
}
