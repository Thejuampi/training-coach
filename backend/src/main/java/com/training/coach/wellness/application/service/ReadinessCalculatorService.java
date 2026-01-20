package com.training.coach.wellness.application.service;

import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.HeartRateVariability;
import com.training.coach.wellness.domain.model.PhysiologicalData;
import com.training.coach.wellness.domain.model.SubjectiveWellness;
import com.training.coach.wellness.domain.model.TrainingLoadSummary;
import org.springframework.stereotype.Service;

/**
 * Readiness calculator that follows Seiler's polarized training principles.
 * 
 * Key principles from coaching science:
 * - Consistency is foundational (missed workouts significantly impact readiness)
 * - Subjective reports are leading indicators (fatigue, soreness)
 * - Physiological markers (HRV, RHR) are lagging indicators
 * - Multiple negative signals compound non-linearly
 */
@Service
public class ReadinessCalculatorService {

    private static final double MAX_READINESS = 100.0;
    private static final double MIN_READINESS = 0.0;

    // Seiler-informed weights: prioritize subjective and consistency over physiology
    private static final double SUBJECTIVE_WEIGHT = 0.40;  // Leading indicators
    private static final double MISSED_WORKOUTS_WEIGHT = 0.15;  // Consistency is #1 principle
    private static final double SLEEP_WEIGHT = 0.20;  // Recovery foundation
    private static final double HRV_WEIGHT = 0.15;  // Lagging indicator
    private static final double RHR_WEIGHT = 0.10;  // Supporting indicator

    private static final double TYPICAL_HRV = 50.0;
    private static final double TYPICAL_RHR = 60.0;
    private static final double OPTIMAL_SLEEP_HOURS = 8.0;
    private static final double PENALTY_PER_MISSED_WORKOUT = 20.0;  // Each missed workout deducts points

    /**
     * Calculate readiness score considering all factors.
     * 
     * @param physiological Physiological metrics (HRV, RHR, sleep)
     * @param subjective Subjective wellness (fatigue, stress, motivation, soreness)
     * @param load Training load summary (TSB context)
     * @param missedWorkouts Number of missed workouts in recent period
     * @return Readiness score 0-100 (lower = less ready to train)
     */
    public double calculateReadiness(
            PhysiologicalData physiological, 
            SubjectiveWellness subjective, 
            TrainingLoadSummary load,
            int missedWorkouts) {
        
        double subjectiveScore = calculateSubjectiveScore(subjective);
        double missedWorkoutsPenalty = calculateMissedWorkoutsPenalty(missedWorkouts);
        double hrvScore = calculateHrvScore(physiological.hrv());
        double rhrScore = calculateRhrScore(physiological.restingHeartRate());
        double sleepScore = calculateSleepScore(physiological.sleep());

        // Combine scores with Seiler-informed weights
        // Note: TSB is not directly used as it's already reflected in subjective fatigue/soreness
        double readiness = (subjectiveScore * SUBJECTIVE_WEIGHT
                + missedWorkoutsPenalty * MISSED_WORKOUTS_WEIGHT
                + sleepScore * SLEEP_WEIGHT
                + hrvScore * HRV_WEIGHT
                + rhrScore * RHR_WEIGHT);

        return clampReadiness(readiness);
    }

    /**
     * Calculate readiness with default zero missed workouts (backward compatibility).
     */
    public double calculateReadiness(
            PhysiologicalData physiological, 
            SubjectiveWellness subjective, 
            TrainingLoadSummary load) {
        return calculateReadiness(physiological, subjective, load, 0);
    }

    /**
     * Calculate penalty for missed workouts.
     * In Seiler's framework, consistency is the #1 principle - missed sessions
     * signal that the athlete is not ready to train at expected capacity.
     * 
     * Formula: Base score (75) minus 35 points per missed workout,
     * with minimum floor at 0.
     */
    private double calculateMissedWorkoutsPenalty(int missedWorkouts) {
        if (missedWorkouts <= 0) {
            return 75.0;  // No penalty, good base score
        }
        // Each missed workout deducts 35 points - this is significant because
        // in Seiler's framework, consistency is foundational
        double penalty = 75.0 - (missedWorkouts * 35.0);
        return Math.max(0.0, penalty);
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
        // Weight fatigue and soreness more heavily as they're key readiness indicators
        double inverseFatigue = (11.0 - subjective.fatigueScore()) / 10.0 * 30.0;
        double inverseStress = (11.0 - subjective.stressScore()) / 10.0 * 20.0;
        double motivation = (subjective.motivationScore() - 1) / 9.0 * 20.0;
        double sorenessPenalty = (10.0 - subjective.muscleSorenessScore()) / 10.0 * 30.0;
        return inverseFatigue + inverseStress + motivation + sorenessPenalty;
    }

    private double clampReadiness(double value) {
        return Math.max(MIN_READINESS, Math.min(MAX_READINESS, value));
    }

    private double clampScore(double value) {
        return Math.max(0.0, Math.min(100.0, value));
    }
}
