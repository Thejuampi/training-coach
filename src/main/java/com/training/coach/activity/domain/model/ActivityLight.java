package com.training.coach.activity.domain.model;

import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.Kilometers;
import com.training.coach.shared.domain.unit.Seconds;
import com.training.coach.shared.domain.unit.Watts;
import java.time.LocalDate;
import java.util.Objects;

public record ActivityLight(
        String id,
        String athleteId,
        String externalActivityId,
        LocalDate date,
        String name,
        String type,
        Seconds durationSeconds,
        Kilometers distanceKm,
        Watts averagePower,
        BeatsPerMinute averageHeartRate,
        Double trainingStressScore,
        Double intensityFactor,
        Watts normalizedPower) {
    public ActivityLight {
        if (athleteId == null || athleteId.isBlank()) {
            throw new IllegalArgumentException("Athlete ID cannot be null or blank");
        }
        if (externalActivityId == null || externalActivityId.isBlank()) {
            throw new IllegalArgumentException("External activity ID cannot be null or blank");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
    }

    public static ActivityLight create(
            String athleteId,
            String externalActivityId,
            LocalDate date,
            String name,
            String type,
            Seconds durationSeconds,
            Kilometers distanceKm,
            Watts averagePower,
            BeatsPerMinute averageHeartRate,
            Double trainingStressScore,
            Double intensityFactor,
            Watts normalizedPower) {
        return new ActivityLight(
                generateId(athleteId, externalActivityId),
                athleteId,
                externalActivityId,
                date,
                name,
                type,
                durationSeconds,
                distanceKm,
                averagePower,
                averageHeartRate,
                trainingStressScore,
                intensityFactor,
                normalizedPower);
    }

    private static String generateId(String athleteId, String externalActivityId) {
        return athleteId + "_" + externalActivityId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ActivityLight that = (ActivityLight) o;
        return Objects.equals(athleteId, that.athleteId) && Objects.equals(externalActivityId, that.externalActivityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(athleteId, externalActivityId);
    }
}
