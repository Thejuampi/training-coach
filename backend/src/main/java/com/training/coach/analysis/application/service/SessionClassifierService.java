package com.training.coach.analysis.application.service;

import com.training.coach.analysis.domain.model.IntensityClassification;
import com.training.coach.analysis.domain.model.Zone;
import com.training.coach.shared.domain.unit.Watts;
import org.springframework.stereotype.Service;

/**
 * Service for classifying workout sessions based on intensity targets.
 */
@Service
public class SessionClassifierService {

    private static final double VO2_OPTIMAL_MIN_PCT = 1.05;
    private static final double VO2_OPTIMAL_MAX_PCT = 1.25;
    private static final double SPRINT_MIN_PCT = 1.30;
    private static final double TEMPO_MIN_PCT = 0.85;
    private static final double TEMPO_MAX_PCT = 0.95;

    /**
     * Classifies an interval session based on %FTP and duration.
     *
     * @param percentFtp the intensity as a percentage of FTP
     * @param durationSeconds the duration of each interval
     * @return the intensity classification
     */
    public IntensityClassification classifyIntervalSession(double percentFtp, int durationSeconds) {
        if (percentFtp >= SPRINT_MIN_PCT && durationSeconds <= 60) {
            return IntensityClassification.SPRINT;
        }
        if (percentFtp >= VO2_OPTIMAL_MIN_PCT && percentFtp <= VO2_OPTIMAL_MAX_PCT
                && durationSeconds >= 180 && durationSeconds <= 600) {
            return IntensityClassification.VO2_OPTIMAL;
        }
        if (percentFtp >= TEMPO_MIN_PCT && percentFtp <= TEMPO_MAX_PCT) {
            return IntensityClassification.THRESHOLD;
        }
        if (percentFtp < TEMPO_MIN_PCT) {
            return IntensityClassification.ENDURANCE;
        }
        return IntensityClassification.THRESHOLD;
    }

    /**
     * Classifies a session based on average power relative to thresholds.
     *
     * @param avgPower the average power of the session
     * @param lt1 the LT1 threshold
     * @param lt2 the LT2 threshold
     * @return the zone classification
     */
    public Zone classifySessionByAveragePower(Watts avgPower, Watts lt1, Watts lt2) {
        if (avgPower.value() <= lt1.value()) {
            return Zone.Z1;
        }
        if (avgPower.value() <= lt2.value()) {
            return Zone.Z2;
        }
        return Zone.Z3;
    }

    /**
     * Determines if a prescription is below LT1 (FATMAX zone).
     *
     * @param percentFtp the intensity as a percentage of FTP
     * @param ftp the functional threshold power
     * @param lt1 the LT1 threshold
     * @return true if the prescription is in the FATMAX zone
     */
    public boolean isBelowLt1(double percentFtp, Watts ftp, Watts lt1) {
        double targetWatts = ftp.value() * percentFtp;
        return targetWatts < lt1.value();
    }

    /**
     * Classifies workout purpose based on session type.
     *
     * @param sessionType the type of session
     * @return the intensity purpose
     */
    public WorkoutIntensityPurpose classifyPurpose(String sessionType) {
        return switch (sessionType.toUpperCase()) {
            case "FATMAX" -> WorkoutIntensityPurpose.Z1_FATMAX;
            case "RECOVERY" -> WorkoutIntensityPurpose.Z1_RECOVERY;
            case "ENDURANCE" -> WorkoutIntensityPurpose.Z1_ENDURANCE;
            case "TEMPO" -> WorkoutIntensityPurpose.Z2_DISCOURAGED_TEMPO;
            case "THRESHOLD" -> WorkoutIntensityPurpose.Z2_THRESHOLD;
            case "INTERVALS", "VO2" -> WorkoutIntensityPurpose.Z3_VO2_OPTIMAL;
            case "SPRINT" -> WorkoutIntensityPurpose.Z3_SPRINT;
            default -> WorkoutIntensityPurpose.Z1_ENDURANCE;
        };
    }
}
