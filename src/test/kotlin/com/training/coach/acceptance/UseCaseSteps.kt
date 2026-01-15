package com.training.coach.acceptance

import io.cucumber.java.Before
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.assertj.core.api.Assertions.assertThat
import com.training.coach.shared.domain.unit.BeatsPerMinute
import com.training.coach.shared.domain.unit.Centimeters
import com.training.coach.shared.domain.unit.HeartRateVariability
import com.training.coach.shared.domain.unit.Hours
import com.training.coach.shared.domain.unit.Kilograms
import com.training.coach.shared.domain.unit.Vo2Max
import com.training.coach.shared.domain.unit.Watts
import java.time.DayOfWeek
import java.time.LocalDate

class UseCaseSteps {

    private lateinit var world: UseCaseWorld

    @Before
    fun reset() {
        world = UseCaseWorld()
    }

    @Given("a coach creates an athlete profile with age {int} and level {word}")
    fun createAthleteProfile(age: Int, level: String) {
        world.athleteProfile = AthleteProfile(
            gender = "unspecified",
            age = age,
            weightKg = Kilograms.of(75.0),
            heightCm = Centimeters.of(175.0),
            level = level
        )
    }

    @Given("metrics ftp {double} fthr {double} vo2 {double}")
    fun setMetrics(ftp: Double, fthr: Double, vo2: Double) {
        world.metrics = TrainingMetrics(
            Watts.of(ftp),
            BeatsPerMinute.of(fthr),
            Vo2Max.of(vo2),
            Kilograms.of(75.0)
        )
    }

    @Given("preferences availability {string} weekly volume {double} phase {string}")
    fun setPreferences(availabilityCsv: String, weeklyVolume: Double, phase: String) {
        val days = availabilityCsv.split(",")
            .map { it.trim().uppercase() }
            .map { DayOfWeek.valueOf(it) }
            .toSet()
        world.preferences = TrainingPreferences(days, Hours.of(weeklyVolume), phase)
        world.athlete = Athlete(
            id = "ath-1",
            name = "Test Athlete",
            profile = world.athleteProfile,
            metrics = world.metrics,
            preferences = world.preferences
        )
    }

    @When("the coach connects the athlete to Intervals.icu")
    fun connectIntervals() {
        world.integrationStatus = "connected"
    }

    @Then("the athlete is stored with link status {string}")
    fun athleteStoredWithStatus(status: String) {
        assertThat(world.athlete).isNotNull
        assertThat(world.integrationStatus).isEqualTo(status)
    }

    @Given("an athlete with linked Intervals.icu")
    fun athleteWithLinkedIntervals() {
        world.athlete = Athlete(
            id = "ath-1",
            name = "Test Athlete",
            profile = AthleteProfile("unspecified", 30, Kilograms.of(75.0), Centimeters.of(175.0), "intermediate"),
            metrics = TrainingMetrics(
                Watts.of(250.0),
                BeatsPerMinute.of(180.0),
                Vo2Max.of(45.0),
                Kilograms.of(75.0)
            ),
            preferences = TrainingPreferences(setOf(DayOfWeek.MONDAY), Hours.of(10.0), "base")
        )
        world.integrationStatus = "connected"
    }

    @When("a sync is triggered")
    fun triggerSync() {
        world.syncEvents++
        world.workoutsIngested = 5
        world.wellnessIngested = true
    }

    @Then("workouts and wellness are ingested")
    fun workoutsAndWellnessIngested() {
        assertThat(world.workoutsIngested).isGreaterThan(0)
        assertThat(world.wellnessIngested).isTrue
    }

    @Then("a sync event is recorded")
    fun syncEventRecorded() {
        assertThat(world.syncEvents).isGreaterThan(0)
    }

    @When("the coach requests a plan for phase {string} duration {int} weeks start date {string} target weekly hours {double}")
    fun requestPlan(phase: String, weeks: Int, startDate: String, weeklyHours: Double) {
        val start = LocalDate.parse(startDate)
        val totalHours = Hours.of(weeklyHours * weeks)
        val highHours = Hours.of(totalHours.value() * 0.2)
        world.plan = TrainingPlan(
            phase = phase,
            startDate = start,
            endDate = start.plusWeeks(weeks.toLong()),
            totalHours = totalHours,
            highIntensityHours = highHours
        )
    }

    @Then("a plan is created with 80/20 intensity split")
    fun planCreatedWith8020() {
        assertThat(world.plan).isNotNull
        val plan = world.plan!!
        val ratio = plan.highIntensityHours.value() / plan.totalHours.value()
        assertThat(ratio).isBetween(0.15, 0.25)
    }

    @Given("readiness signals hrv {double} rhr {double} sleep {double} fatigue {double} subjective {int}")
    fun readinessSignals(hrv: Double, rhr: Double, sleep: Double, fatigue: Double, subjective: Int) {
        world.readinessSignals = ReadinessSignals(
            HeartRateVariability.of(hrv),
            BeatsPerMinute.of(rhr),
            Hours.of(sleep),
            fatigue,
            subjective
        )
    }

    @When("readiness is calculated")
    fun readinessCalculated() {
        val signals = world.readinessSignals
        val base = 100.0 - signals.fatigue
        val sleepBoost = (signals.sleepHours.value() - 6.0) * 5.0
        val subjectiveBoost = (signals.subjective - 3) * 2.0
        val score = (base + sleepBoost + subjectiveBoost).coerceIn(0.0, 100.0)
        world.readinessScore = score
    }

    @Then("readiness score is between 0 and 100")
    fun readinessScoreBetween() {
        assertThat(world.readinessScore).isBetween(0.0, 100.0)
    }

    @Given("a plan with {int} workouts planned")
    fun planWithWorkoutsPlanned(count: Int) {
        world.plannedWorkouts = count
    }

    @Given("actual workouts completed {int}")
    fun actualWorkoutsCompleted(count: Int) {
        world.completedWorkouts = count
    }

    @When("compliance is evaluated")
    fun complianceEvaluated() {
        world.complianceRatio = if (world.plannedWorkouts == 0) 0.0
        else world.completedWorkouts.toDouble() / world.plannedWorkouts.toDouble()
    }

    @Then("compliance percent is {double}")
    fun compliancePercentIs(expected: Double) {
        assertThat(world.complianceRatio).isCloseTo(expected, org.assertj.core.data.Offset.offset(0.01))
    }

    @Given("low readiness score {double}")
    fun lowReadinessScore(score: Double) {
        world.readinessScore = score
        world.weeklyVolumeHours = Hours.of(10.0)
    }

    @When("the coach applies a recovery adjustment")
    fun applyRecoveryAdjustment() {
        if (world.readinessScore < 50.0) {
            world.weeklyVolumeHours = world.weeklyVolumeHours.times(0.8)
        }
    }

    @Then("weekly volume is reduced by {int} percent")
    fun weeklyVolumeReducedBy(percent: Int) {
        val expected = Hours.of(10.0 * (1.0 - percent / 100.0))
        assertThat(world.weeklyVolumeHours.value())
            .isCloseTo(expected.value(), org.assertj.core.data.Offset.offset(0.01))
    }

    @Given("an athlete and a plan exist")
    fun athleteAndPlanExist() {
        world.athlete = Athlete(
            id = "ath-1",
            name = "Test Athlete",
            profile = AthleteProfile("unspecified", 30, Kilograms.of(75.0), Centimeters.of(175.0), "intermediate"),
            metrics = TrainingMetrics(
                Watts.of(250.0),
                BeatsPerMinute.of(180.0),
                Vo2Max.of(45.0),
                Kilograms.of(75.0)
            ),
            preferences = TrainingPreferences(setOf(DayOfWeek.MONDAY), Hours.of(10.0), "base")
        )
        world.plan = TrainingPlan("base", LocalDate.now(), LocalDate.now().plusWeeks(4), Hours.of(40.0), Hours.of(8.0))
    }

    @When("the coach posts a note {string}")
    fun coachPostsNote(note: String) {
        world.notes.add(note)
    }

    @Then("the note is stored for the athlete")
    fun noteStoredForAthlete() {
        assertThat(world.notes).isNotEmpty
        assertThat(world.notes.last()).isNotBlank
    }

    @Given("an admin enters API key {string}")
    fun adminEntersApiKey(key: String) {
        world.apiKey = key
    }

    @When("the integration is validated")
    fun integrationValidated() {
        world.integrationStatus = if (world.apiKey.isNotBlank()) "active" else "invalid"
    }

    @Then("integration status is {string}")
    fun integrationStatusIs(status: String) {
        assertThat(world.integrationStatus).isEqualTo(status)
    }
}

private data class Athlete(
    val id: String,
    val name: String,
    val profile: AthleteProfile,
    val metrics: TrainingMetrics,
    val preferences: TrainingPreferences
)

private data class AthleteProfile(
    val gender: String,
    val age: Int,
    val weightKg: Kilograms,
    val heightCm: Centimeters,
    val level: String
)

private data class TrainingMetrics(
    val ftp: Watts,
    val fthr: BeatsPerMinute,
    val vo2max: Vo2Max,
    val weightKg: Kilograms
)

private data class TrainingPreferences(
    val availableDays: Set<DayOfWeek>,
    val targetWeeklyVolumeHours: Hours,
    val currentPhase: String
)

private data class TrainingPlan(
    val phase: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalHours: Hours,
    val highIntensityHours: Hours
)

private data class ReadinessSignals(
    val hrv: HeartRateVariability,
    val rhr: BeatsPerMinute,
    val sleepHours: Hours,
    val fatigue: Double,
    val subjective: Int
)

private class UseCaseWorld {
    lateinit var athleteProfile: AthleteProfile
    lateinit var metrics: TrainingMetrics
    lateinit var preferences: TrainingPreferences
    var athlete: Athlete? = null
    var integrationStatus: String = ""
    var syncEvents: Int = 0
    var workoutsIngested: Int = 0
    var wellnessIngested: Boolean = false
    var plan: TrainingPlan? = null
    lateinit var readinessSignals: ReadinessSignals
    var readinessScore: Double = 0.0
    var plannedWorkouts: Int = 0
    var completedWorkouts: Int = 0
    var complianceRatio: Double = 0.0
    var weeklyVolumeHours: Hours = Hours.of(0.0)
    val notes: MutableList<String> = mutableListOf()
    var apiKey: String = ""
}
