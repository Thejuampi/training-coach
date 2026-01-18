package com.training.coach.athlete.domain.model;

import java.util.Objects;

public record Athlete(
        String id,
        String name,
        AthleteProfile profile,
        TrainingMetrics currentMetrics,
        TrainingPreferences preferences) {
    public Athlete withUpdatedMetrics(TrainingMetrics newMetrics) {
        return new Athlete(id, name, profile, newMetrics, preferences);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Athlete athlete = (Athlete) o;
        return Objects.equals(id, athlete.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
