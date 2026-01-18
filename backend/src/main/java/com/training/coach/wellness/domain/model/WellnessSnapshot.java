package com.training.coach.wellness.domain.model;

import java.time.LocalDate;
import java.util.Objects;

public record WellnessSnapshot(
        String id,
        String athleteId,
        LocalDate date,
        PhysiologicalData physiological,
        SubjectiveWellness subjective,
        TrainingLoadSummary loadSummary,
        double readinessScore) {
    public WellnessSnapshot {
        if (athleteId == null || athleteId.isBlank()) {
            throw new IllegalArgumentException("Athlete ID cannot be null or blank");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (readinessScore < 0 || readinessScore > 100) {
            throw new IllegalArgumentException("Readiness score must be between 0 and 100");
        }
    }

    public static WellnessSnapshot create(
            String athleteId,
            LocalDate date,
            PhysiologicalData physiological,
            SubjectiveWellness subjective,
            TrainingLoadSummary loadSummary,
            double readinessScore) {
        return new WellnessSnapshot(
                generateId(athleteId, date), athleteId, date, physiological, subjective, loadSummary, readinessScore);
    }

    private static String generateId(String athleteId, LocalDate date) {
        return athleteId + "_" + date.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WellnessSnapshot that = (WellnessSnapshot) o;
        return Objects.equals(athleteId, that.athleteId) && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(athleteId, date);
    }
}
