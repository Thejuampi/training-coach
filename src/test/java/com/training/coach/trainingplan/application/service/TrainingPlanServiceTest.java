package com.training.coach.trainingplan.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.training.coach.athlete.domain.model.Athlete;
import com.training.coach.athlete.domain.model.AthleteProfile;
import com.training.coach.athlete.domain.model.TrainingMetrics;
import com.training.coach.athlete.domain.model.TrainingPlan;
import com.training.coach.athlete.domain.model.TrainingPreferences;
import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.Centimeters;
import com.training.coach.shared.domain.unit.Hours;
import com.training.coach.shared.domain.unit.Kilograms;
import com.training.coach.shared.domain.unit.Vo2Max;
import com.training.coach.shared.domain.unit.Watts;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Training Plan Service Tests")
class TrainingPlanServiceTest {

    @Test
    @DisplayName("Should generate polarized training plan with 80/20 volume split")
    void shouldGeneratePolarizedTrainingPlan() {
        // Given
        Athlete athlete = createTestAthlete();
        TrainingPlanService service = new TrainingPlanService();

        // When
        TrainingPlan plan = service.generatePlan(athlete, "base", LocalDate.now(), Hours.of(10.0));

        // Then
        assertThat(plan).isNotNull();
        assertThat(plan.workouts()).isNotEmpty();
        // Check 80/20: high intensity ~20%, low ~80%
        double highVolume = plan.workouts().stream()
                .filter(w -> w.type() == com.training.coach.athlete.domain.model.Workout.WorkoutType.INTERVALS
                        || w.type() == com.training.coach.athlete.domain.model.Workout.WorkoutType.THRESHOLD)
                .mapToDouble(w -> w.durationMinutes().asHours())
                .sum();
        double totalVolume = plan.totalVolumeHours().value();
        assertThat(highVolume / totalVolume).isLessThanOrEqualTo(0.25); // Allow some tolerance
    }

    private Athlete createTestAthlete() {
        AthleteProfile profile =
                new AthleteProfile("male", 30, Kilograms.of(75.0), Centimeters.of(175.0), "intermediate");
        TrainingMetrics metrics =
                new TrainingMetrics(Watts.of(250.0), BeatsPerMinute.of(180.0), Vo2Max.of(45.0), Kilograms.of(75.0));
        Set<DayOfWeek> availableDays = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
        TrainingPreferences preferences = new TrainingPreferences(availableDays, Hours.of(10.0), "base");
        return new Athlete("test-id", "Test Athlete", profile, metrics, preferences);
    }
}
