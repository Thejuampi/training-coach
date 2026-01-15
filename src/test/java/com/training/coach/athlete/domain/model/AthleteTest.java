package com.training.coach.athlete.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.Centimeters;
import com.training.coach.shared.domain.unit.Hours;
import com.training.coach.shared.domain.unit.Kilograms;
import com.training.coach.shared.domain.unit.Vo2Max;
import com.training.coach.shared.domain.unit.Watts;
import java.time.DayOfWeek;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Athlete Domain Model Tests")
class AthleteTest {

    @Test
    @DisplayName("Should create athlete with valid data")
    void shouldCreateAthleteWithValidData() {
        // Given: valid athlete data
        String athleteId = "athlete-123";
        String name = "John Doe";
        AthleteProfile profile =
                new AthleteProfile("male", 30, Kilograms.of(75.0), Centimeters.of(175.0), "intermediate");
        TrainingMetrics metrics =
                new TrainingMetrics(Watts.of(250.0), BeatsPerMinute.of(180.0), Vo2Max.of(45.0), Kilograms.of(150.0));
        TrainingPreferences preferences = new TrainingPreferences(
                Set.of(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY,
                        DayOfWeek.SATURDAY,
                        DayOfWeek.SUNDAY),
                Hours.of(10.0),
                "base");

        // When: creating athlete
        Athlete athlete = new Athlete(athleteId, name, profile, metrics, preferences);

        // Then: athlete should be created with all data
        assertThat(athlete.id()).isEqualTo(athleteId);
        assertThat(athlete.name()).isEqualTo(name);
        assertThat(athlete.profile()).isEqualTo(profile);
        assertThat(athlete.currentMetrics()).isEqualTo(metrics);
        assertThat(athlete.preferences()).isEqualTo(preferences);
    }

    @Test
    @DisplayName("Should update athlete metrics")
    void shouldUpdateAthleteMetrics() {
        // Given: an athlete with current metrics
        Athlete athlete = createTestAthlete();
        TrainingMetrics newMetrics =
                new TrainingMetrics(Watts.of(260.0), BeatsPerMinute.of(175.0), Vo2Max.of(46.0), Kilograms.of(155.0));

        // When: updating metrics
        Athlete updatedAthlete = athlete.withUpdatedMetrics(newMetrics);

        // Then: metrics should be updated
        assertThat(updatedAthlete.currentMetrics()).isEqualTo(newMetrics);
        assertThat(updatedAthlete.id()).isEqualTo(athlete.id());
        assertThat(updatedAthlete.name()).isEqualTo(athlete.name());
    }

    @Test
    @DisplayName("Should validate training preferences")
    void shouldValidateTrainingPreferences() {
        // Given: valid training preferences
        TrainingPreferences preferences = new TrainingPreferences(
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), Hours.of(10.0), "base");

        // Expect: preferences should be valid
        assertThat(preferences.availableDays().size()).isEqualTo(3);
        assertThat(preferences.targetWeeklyVolumeHours()).isEqualTo(Hours.of(10.0));
        assertThat(preferences.currentPhase()).isEqualTo("base");
    }

    private Athlete createTestAthlete() {
        return new Athlete(
                "athlete-123",
                "John Doe",
                new AthleteProfile("male", 30, Kilograms.of(75.0), Centimeters.of(175.0), "intermediate"),
                new TrainingMetrics(Watts.of(250.0), BeatsPerMinute.of(180.0), Vo2Max.of(45.0), Kilograms.of(150.0)),
                new TrainingPreferences(
                        Set.of(
                                DayOfWeek.MONDAY,
                                DayOfWeek.TUESDAY,
                                DayOfWeek.WEDNESDAY,
                                DayOfWeek.THURSDAY,
                                DayOfWeek.FRIDAY,
                                DayOfWeek.SATURDAY,
                                DayOfWeek.SUNDAY),
                        Hours.of(10.0),
                        "base"));
    }
}
