package com.training.coach.trainingplan.application.service

import com.training.coach.athlete.domain.model.Athlete
import com.training.coach.athlete.domain.model.AthleteProfile
import com.training.coach.athlete.domain.model.TrainingMetrics
import com.training.coach.athlete.domain.model.TrainingPreferences
import com.training.coach.shared.domain.unit.BeatsPerMinute
import com.training.coach.shared.domain.unit.Centimeters
import com.training.coach.shared.domain.unit.Hours
import com.training.coach.shared.domain.unit.Kilograms
import com.training.coach.shared.domain.unit.Vo2Max
import com.training.coach.shared.domain.unit.Watts
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate

class TrainingPlanServiceTest {

    @Test
    fun `should generate polarized training plan with 80/20 volume split`() {
        val athlete = createTestAthlete()
        val service = TrainingPlanService()

        val plan = service.generatePlan(athlete, "base", LocalDate.now(), Hours.of(10.0))

        assertThat(plan).isNotNull
        assertThat(plan.workouts()).isNotEmpty

        val highVolume = plan.workouts()
            .filter { it.type().name == "INTERVALS" || it.type().name == "THRESHOLD" }
            .sumOf { it.durationMinutes().asHours() }
        val totalVolume = plan.totalVolumeHours().value()

        assertThat(highVolume / totalVolume).isLessThanOrEqualTo(0.25)
    }

    private fun createTestAthlete(): Athlete {
        val profile = AthleteProfile("male", 30, Kilograms.of(75.0), Centimeters.of(175.0), "intermediate")
        val metrics = TrainingMetrics(Watts.of(250.0), BeatsPerMinute.of(180.0), Vo2Max.of(45.0), Kilograms.of(75.0))
        val preferences = TrainingPreferences(
            setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            Hours.of(10.0),
            "base"
        )
        return Athlete("test-id", "Test Athlete", profile, metrics, preferences)
    }
}
