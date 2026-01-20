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
 * Key principles from coaching science (Seiler, Foster, Banister):
 * - Subjective perception of recovery (fatigue, soreness) correlates MORE strongly 
 *   with performance than HRV/RHR on a single-day basis
 * - HRV/RHR are lagging indicators - they respond to systemic stress but not 
 *   local muscular damage or cumulative strain
 * - "Don't train hard when you feel bad" - protect high days by respecting low days
 * - Missed workouts indicate rhythm disruption and should penalize readiness
 * 
 * @see <a href="https://journals.humankinetics.com/view/journals/tsac/28/3/article-p265.xml">Seiler's work on overreaching and recovery monitoring</a>
 */
@Service
public class ReadinessCalculatorService {

    private static final double MAX_READINESS = 100.0;
    private static final double MIN_READINESS = 0.0;

    // Evidence-based weights aligned with Seiler's framework
    private static final double SUBJECTIVE_WEIGHT = 0.40;  // Highest correlation with readiness
    private static final double SLEEP_WEIGHT = 0.20;       // Recovery foundation
    private static final double COMPLIANCE_WEIGHT = 0.15;  // Missed workouts, load integration
    private static final double HRV_WEIGHT = 0.15;         // Moderate, trends matter more
    private static final double RHR_WEIGHT = 0.10;         // Secondary confirmation

    // Thresholds for override rules
    private static final int HIGH_FATIGUE_THRESHOLD = 8;
    private static final int HIGH_SORENESS_THRESHOLD = 8;
    private static final int LOW_MOOD_THRESHOLD = 5;
    private static final double POOR_SLEEP_HOURS_THRESHOLD = 6.0;
    private static final int POOR_SLEEP_QUALITY_THRESHOLD = 4;

    // Penalties
    private static final double PENALTY_PER_MISSED_WORKOUT = 5.0;
    private static final double POOR_SLEEP_PENALTY = 10.0;
    private static final double LOW_MOOD_PENALTY = 5.0;
    private static final double HIGH_FATIGUE_SORENESS_CAP = 40.0;

    private static final double TYPICAL_HRV = 50.0;
    private static final double TYPICAL_RHR = 60.0;
    private static final double OPTIMAL_SLEEP_HOURS = 8.0;

    /**
     * Calculate readiness score considering all factors with Seiler-informed weighting.
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
        
        // Calculate base scores
        double subjectiveScore = calculateSubjectiveScore(subjective);
        double sleepScore = calculateSleepScore(physiological.sleep());
        double hrvScore = calculateHrvScore(physiological.hrv());
        double rhrScore = calculateRhrScore(physiological.restingHeartRate());
        double complianceScore = calculateComplianceScore(missedWorkouts);

        // Calculate weighted base readiness
        double readiness = (subjectiveScore * SUBJECTIVE_WEIGHT
                + sleepScore * SLEEP_WEIGHT
                + hrvScore * HRV_WEIGHT
                + rhrScore * RHR_WEIGHT
                + complianceScore * COMPLIANCE_WEIGHT);

        // Apply override rules based on coach's evidence-based guidelines
        readiness = applyOverrideRules(readiness, subjective, physiological.sleep(), missedWorkouts);

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
     * Apply coaching science override rules.
     * 
     * Rules based on Seiler's guidance:
     * 1. If fatigue ≥ 8 AND soreness ≥ 8 → cap at 40 (acute overreaching)
     * 2. Poor sleep (<6h or quality ≤4) → -10 penalty
     * 3. Missed workout → -5 penalty (rhythm disruption)
     * 4. Low mood (<5) → -5 penalty (motivation/injury risk)
     */
    private double applyOverrideRules(
            double readiness,
            SubjectiveWellness subjective,
            com.training.coach.wellness.domain.model.SleepMetrics sleep,
            int missedWorkouts) {
        
        if (subjective == null) {
            return readiness;  // Cannot apply rules without subjective data
        }

        double adjusted = readiness;

        // Rule 1: Cap at 40 if high fatigue AND high soreness
        // This reflects that HRV/RHR may be normal but functional readiness is impaired
        if (subjective.fatigueScore() >= HIGH_FATIGUE_THRESHOLD && 
            subjective.muscleSorenessScore() >= HIGH_SORENESS_THRESHOLD) {
            adjusted = Math.min(adjusted, HIGH_FATIGUE_SORENESS_CAP);
        }

        // Rule 2: Poor sleep penalty
        if (sleep != null) {
            if (sleep.totalSleepHours().value() < POOR_SLEEP_HOURS_THRESHOLD ||
                sleep.qualityScore() <= POOR_SLEEP_QUALITY_THRESHOLD) {
                adjusted -= POOR_SLEEP_PENALTY;
            }
        }

        // Rule 3: Missed workout penalty (rhythm disruption)
        if (missedWorkouts > 0) {
            adjusted -= (missedWorkouts * PENALTY_PER_MISSED_WORKOUT);
        }

        // Rule 4: Low mood penalty
        if (subjective.motivationScore() < LOW_MOOD_THRESHOLD) {
            adjusted -= LOW_MOOD_PENALTY;
        }

        return adjusted;
    }

    /**
     * Calculate compliance score based on missed workouts.
     * In Seiler's framework, consistency is foundational.
     * Missed sessions signal the athlete is not ready to train at expected capacity.
     */
    private double calculateComplianceScore(int missedWorkouts) {
        if (missedWorkouts <= 0) {
            return 75.0;  // No penalty, good base score for consistency
        }
        // Base score reduced by missed workouts
        double score = 75.0 - (missedWorkouts * 10.0);
        return Math.max(0.0, score);
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
        
        // Invert scales so high fatigue/soreness = low score (bad)
        // Fatigue 8/10 → 20/100, Soreness 9/10 → 10/100
        double inverseFatigue = (11.0 - subjective.fatigueScore()) / 10.0 * 30.0;
        double inverseSoreness = (11.0 - subjective.muscleSorenessScore()) / 10.0 * 30.0;
        
        // Stress inversely weighted (high stress = lower readiness)
        double inverseStress = (11.0 - subjective.stressScore()) / 10.0 * 20.0;
        
        // Motivation directly weighted (high motivation = higher readiness)
        double motivation = (subjective.motivationScore() - 1) / 9.0 * 20.0;
        
        return inverseFatigue + inverseSoreness + inverseStress + motivation;
    }

    private double clampReadiness(double value) {
        return Math.max(MIN_READINESS, Math.min(MAX_READINESS, value));
    }

    private double clampScore(double value) {
        return Math.max(0.0, Math.min(100.0, value));
    }
}
