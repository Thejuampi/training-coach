package com.training.coach.acceptance

import com.training.coach.athlete.domain.model.Athlete
import com.training.coach.athlete.domain.model.AthleteProfile
import com.training.coach.athlete.domain.model.TrainingMetrics
import com.training.coach.athlete.domain.model.TrainingPreferences
import com.training.coach.shared.domain.unit.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.util.*

class InMemoryAthleteRepositoryTest {

    private val repository = InMemoryAthleteRepository()

    @Test
    fun `save should store and return athlete`() {
        val athlete = createTestAthlete("athlete-1")

        val saved = repository.save(athlete)

        assertThat(saved).isEqualTo(athlete)
        assertThat(repository.findById("athlete-1")).isEqualTo(Optional.of(athlete))
    }

    @Test
    fun `findById should return athlete when exists`() {
        val athlete = createTestAthlete("athlete-1")
        repository.save(athlete)

        val found = repository.findById("athlete-1")

        assertThat(found).isPresent
        assertThat(found.get()).isEqualTo(athlete)
    }

    @Test
    fun `findById should return empty when not exists`() {
        val found = repository.findById("non-existent")

        assertThat(found).isEmpty
    }

    @Test
    fun `deleteById should remove athlete`() {
        val athlete = createTestAthlete("athlete-1")
        repository.save(athlete)

        repository.deleteById("athlete-1")

        assertThat(repository.findById("athlete-1")).isEmpty
    }

    @Test
    fun `deleteById should do nothing when not exists`() {
        repository.deleteById("non-existent")

        // No assertion needed, just ensure no exception
    }

    @Test
    fun `findAll should return all athletes`() {
        val athlete1 = createTestAthlete("athlete-1")
        val athlete2 = createTestAthlete("athlete-2")
        repository.save(athlete1)
        repository.save(athlete2)

        val all = repository.findAll()

        assertThat(all).containsExactlyInAnyOrder(athlete1, athlete2)
    }

    @Test
    fun `findAll should return empty when no athletes`() {
        val all = repository.findAll()

        assertThat(all).isEmpty()
    }

    @Test
    fun `concurrent saves should be thread safe`() {
        val saveTask = Runnable {
            for (i in 0..99) {
                val athlete = createTestAthlete("athlete-${Thread.currentThread().id}-$i")
                repository.save(athlete)
            }
        }

        val t1 = Thread(saveTask)
        val t2 = Thread(saveTask)

        t1.start()
        t2.start()
        t1.join()
        t2.join()

        // Should have 200 athletes
        assertThat(repository.findAll()).hasSize(200)
    }

    private fun createTestAthlete(id: String): Athlete {
        val profile = AthleteProfile("Male", 30, Kilograms.of(70.0), Centimeters.of(180.0), "Intermediate")
        val metrics = TrainingMetrics(Watts.of(250.0), BeatsPerMinute.of(170.0), Vo2Max.of(45.0), Kilograms.of(70.0))
        val preferences = TrainingPreferences(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), Hours.of(10.0), "Build")
        return Athlete(id, "Test Athlete", profile, metrics, preferences)
    }
}