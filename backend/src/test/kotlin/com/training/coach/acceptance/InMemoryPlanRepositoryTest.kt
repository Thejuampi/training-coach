package com.training.coach.acceptance

import com.training.coach.athlete.domain.model.Workout
import com.training.coach.shared.domain.unit.*
import com.training.coach.testconfig.inmemory.InMemoryPlanRepository
import com.training.coach.trainingplan.domain.model.PlanSummary
import com.training.coach.trainingplan.domain.model.PlanVersion
import com.training.coach.trainingplan.infrastructure.persistence.entity.PlanVersionStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.util.*

class InMemoryPlanRepositoryTest {

    private val repository = InMemoryPlanRepository()

    @Test
    fun `save should store and return plan summary`() {
        val plan = createTestPlanSummary("plan-1")
        StringTokenizer
        val saved = repository.save(plan)

        assertThat(saved).isEqualTo(plan)
        assertThat(repository.findById("plan-1")).isEqualTo(Optional.of(plan))
    }

    @Test
    fun `findById should return plan when exists`() {
        val plan = createTestPlanSummary("plan-1")
        repository.save(plan)

        val found = repository.findById("plan-1")

        assertThat(found).isPresent
        assertThat(found.get()).isEqualTo(plan)
    }

    @Test
    fun `findById should return empty when not exists`() {
        val found = repository.findById("non-existent")

        assertThat(found).isEmpty
    }

    @Test
    fun `findAll should return all plans`() {
        val plan1 = createTestPlanSummary("plan-1")
        val plan2 = createTestPlanSummary("plan-2")
        repository.save(plan1)
        repository.save(plan2)

        val all = repository.findAll()

        assertThat(all).containsExactlyInAnyOrder(plan1, plan2)
    }

    @Test
    fun `saveVersion should store version`() {
        val version = createTestPlanVersion("plan-1", 1)

        val saved = repository.saveVersion(version)

        assertThat(saved).isEqualTo(version)
        assertThat(repository.findVersion("plan-1", 1)).isEqualTo(Optional.of(version))
    }

    @Test
    fun `findVersion should return version when exists`() {
        val version = createTestPlanVersion("plan-1", 1)
        repository.saveVersion(version)

        val found = repository.findVersion("plan-1", 1)

        assertThat(found).isPresent
        assertThat(found.get()).isEqualTo(version)
    }

    @Test
    fun `findVersion should return empty when not exists`() {
        val found = repository.findVersion("plan-1", 1)

        assertThat(found).isEmpty
    }

    @Test
    fun `findVersions should return all versions for plan`() {
        val version1 = createTestPlanVersion("plan-1", 1)
        val version2 = createTestPlanVersion("plan-1", 2)
        repository.saveVersion(version1)
        repository.saveVersion(version2)
        // Different plan
        repository.saveVersion(createTestPlanVersion("plan-2", 1))

        val versions = repository.findVersions("plan-1")

        assertThat(versions).containsExactly(version1, version2)
    }

    @Test
    fun `findVersions should return empty when no versions`() {
        val versions = repository.findVersions("plan-1")

        assertThat(versions).isEmpty()
    }

    @Test
    fun `updateVersionStatus should update status`() {
        val version = createTestPlanVersion("plan-1", 1)
        repository.saveVersion(version)

        repository.updateVersionStatus("plan-1", 1, PlanVersionStatus.PUBLISHED)

        val updated = repository.findVersion("plan-1", 1).get()
        assertThat(updated.status()).isEqualTo(PlanVersionStatus.PUBLISHED)
    }

    @Test
    fun `updateVersionStatus should do nothing when version not exists`() {
        repository.updateVersionStatus("plan-1", 1, PlanVersionStatus.PUBLISHED)

        // No exception, no change
    }

    @Test
    fun `filtering active and archived versions`() {
        val activeVersion = createTestPlanVersion("plan-1", 1, PlanVersionStatus.PUBLISHED)
        val archivedVersion = createTestPlanVersion("plan-1", 2, PlanVersionStatus.ARCHIVED)
        repository.saveVersion(activeVersion)
        repository.saveVersion(archivedVersion)

        val versions = repository.findVersions("plan-1")

        val active = versions.filter { it.status() == PlanVersionStatus.PUBLISHED }
        val archived = versions.filter { it.status() == PlanVersionStatus.ARCHIVED }

        assertThat(active).hasSize(1)
        assertThat(archived).hasSize(1)
    }

    @Test
    fun `concurrent operations should be thread safe`() {
        val saveTask = Runnable {
            for (i in 0..49) {
                val plan = createTestPlanSummary("plan-${Thread.currentThread().id}-$i")
                repository.save(plan)
                val version = createTestPlanVersion(plan.id(), 1)
                repository.saveVersion(version)
            }
        }

        val t1 = Thread(saveTask)
        val t2 = Thread(saveTask)

        t1.start()
        t2.start()
        t1.join()
        t2.join()

        // Should have 100 plans and 100 versions
        assertThat(repository.findAll()).hasSize(100)
        // Check one plan has version
        val somePlan = repository.findAll().first()
        assertThat(repository.findVersions(somePlan.id())).hasSize(1)
    }

    private fun createTestPlanSummary(id: String): PlanSummary {
        return PlanSummary(id, "athlete-1", 1, PlanVersionStatus.DRAFT, Instant.now(), null)
    }

    private fun createTestPlanVersion(planId: String, version: Int, status: PlanVersionStatus = PlanVersionStatus.DRAFT): PlanVersion {
        val workout = createTestWorkout()
        return PlanVersion(planId, version, status, listOf(workout), Instant.now())
    }

    private fun createTestWorkout(): Workout {
        val intensityProfile = Workout.IntensityProfile(
            Percent.of(50.0), Percent.of(30.0), Percent.of(20.0), Percent.of(0.0), Percent.of(0.0)
        )
        val interval = Workout.Interval(
            Workout.Interval.IntervalType.THRESHOLD,
            Minutes.of(60),
            Watts.of(200.0),
            BeatsPerMinute.of(160.0)
        )
        return Workout(
            "workout-1",
            LocalDate.now(),
            Workout.WorkoutType.ENDURANCE,
            Minutes.of(60),
            intensityProfile,
            listOf(interval)
        )
    }
}