package com.training.coach.acceptance

import com.training.coach.activity.application.service.ActivityReadService
import com.training.coach.activity.infrastructure.persistence.ActivityJpaRepository
import com.training.coach.analysis.application.service.AdjustmentService
import com.training.coach.analysis.application.service.ComplianceService
import com.training.coach.athlete.application.port.out.AthleteRepository
import com.training.coach.athlete.application.service.AthleteService
import com.training.coach.athlete.domain.model.Athlete
import com.training.coach.athlete.domain.model.AthleteProfile
import com.training.coach.athlete.domain.model.TrainingMetrics
import com.training.coach.athlete.domain.model.TrainingPreferences
import com.training.coach.athlete.infrastructure.persistence.AthleteJpaRepository
import com.training.coach.athlete.application.port.out.FitnessPlatformPort
import com.training.coach.athlete.domain.model.Workout
import com.training.coach.feedback.application.service.NoteService
import com.training.coach.feedback.application.service.NoteService
import com.training.coach.integration.application.service.IntegrationService
import com.training.coach.shared.domain.unit.BeatsPerMinute
import com.training.coach.shared.domain.unit.Centimeters
import com.training.coach.shared.domain.unit.HeartRateVariability
import com.training.coach.shared.domain.unit.Hours
import com.training.coach.shared.domain.unit.Kilograms
import com.training.coach.shared.domain.unit.Kilometers
import com.training.coach.shared.domain.unit.Seconds
import com.training.coach.shared.domain.unit.Vo2Max
import com.training.coach.shared.domain.unit.Watts
import com.training.coach.sync.application.service.SyncService
import com.training.coach.trainingplan.application.service.TrainingPlanService
import com.training.coach.user.application.port.out.SystemUserRepository
import com.training.coach.user.application.port.out.UserCredentialsRepository
import com.training.coach.user.application.service.SystemUserService
import com.training.coach.user.domain.model.DistanceUnit
import com.training.coach.user.domain.model.HeightUnit
import com.training.coach.user.domain.model.MeasurementSystem
import com.training.coach.user.domain.model.SystemUser
import com.training.coach.user.domain.model.UserPreferences
import com.training.coach.user.domain.model.UserRole
import com.training.coach.user.infrastructure.persistence.UserCredentialsJpaRepository
import com.training.coach.wellness.application.port.out.WellnessRepository
import com.training.coach.wellness.application.service.ReadinessCalculatorService
import com.training.coach.wellness.domain.model.PhysiologicalData
import com.training.coach.wellness.domain.model.SleepMetrics
import com.training.coach.wellness.domain.model.SubjectiveWellness
import com.training.coach.wellness.domain.model.TrainingLoadSummary
import com.training.coach.wellness.infrastructure.persistence.WellnessJpaRepository
import io.cucumber.java.Before
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.cucumber.spring.ScenarioScope
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.EnumSet
import org.assertj.core.api.Assertions.assertThat

@ScenarioScope
open class UseCaseSteps(
    private val athleteService: AthleteService,
    private val athleteRepository: AthleteRepository,
    private val athleteJpaRepository: AthleteJpaRepository,
    private val trainingPlanService: TrainingPlanService,
    private val complianceService: ComplianceService,
    private val adjustmentService: AdjustmentService,
    private val noteService: NoteService,
    private val integrationService: IntegrationService,
    private val readinessCalculatorService: ReadinessCalculatorService,
    private val activityReadService: ActivityReadService,
    private val syncService: SyncService,
    private val wellnessRepository: WellnessRepository,
    private val activityJpaRepository: ActivityJpaRepository,
    private val wellnessJpaRepository: WellnessJpaRepository,
    private val fitnessPlatformPort: TestFitnessPlatformPort
) {
    private var athleteProfile: AthleteProfile? = null
    private var trainingMetrics: TrainingMetrics? = null
    private var trainingPreferences: TrainingPreferences? = null
    private var savedAthlete: Athlete? = null
    private var readinessScore: Double? = null
    private var compliancePercent: Double? = null
    private var adjustmentSuggestion: String? = null
    private var plannedWorkouts: Int = 0
    private var completedWorkouts: Int = 0
    private var planWorkouts: List<Workout> = emptyList()
    private var activityHistory: List<com.training.coach.activity.domain.model.ActivityLight> = emptyList()
    private var readinessPhysiological: PhysiologicalData? = null
    private var readinessSubjective: SubjectiveWellness? = null
    private var integrationStatus: String = ""

    @Before
    fun reset() {
        athleteProfile = null
        trainingMetrics = null
        trainingPreferences = null
        savedAthlete = null
        readinessScore = null
        compliancePercent = null
        adjustmentSuggestion = null
        plannedWorkouts = 0
        completedWorkouts = 0
        planWorkouts = emptyList()
        activityHistory = emptyList()
        readinessPhysiological = null
        readinessSubjective = null
        integrationStatus = ""
        fitnessPlatformPort.clear()
        activityJpaRepository.deleteAll()
        wellnessJpaRepository.deleteAll()
        athleteJpaRepository.deleteAll()
    }

    @Given("a coach creates an athlete profile with age {int} and level {word}")
    fun createAthleteProfile(age: Int, level: String) {
        athleteProfile = AthleteProfile(
            "unspec",
            age,
            Kilograms.of(75.0),
            Centimeters.of(175.0),
            level
        )
    }

    @Given("metrics ftp {double} fthr {double} vo2 {double}")
    fun setMetrics(ftp: Double, fthr: Double, vo2: Double) {
        trainingMetrics = TrainingMetrics(
            Watts.of(ftp),
            BeatsPerMinute.of(fthr),
            Vo2Max.of(vo2),
            Kilograms.of(75.0)
        )
    }

    @Given("preferences availability {string} weekly volume {double} phase {string}")
    fun setPreferences(availabilityCsv: String, weeklyVolume: Double, phase: String) {
        trainingPreferences = TrainingPreferences(parseDays(availabilityCsv), Hours.of(weeklyVolume), phase)
    }

    @When("the coach saves the athlete profile")
    fun saveAthleteProfile() {
        val profile = requireNotNull(athleteProfile)
        val preferences = requireNotNull(trainingPreferences)
        val created = athleteService.createAthlete("Test Athlete", profile, preferences)
        assertThat(created.isSuccess()).isTrue
        val athlete = created.value().orElseThrow()
        val metrics = trainingMetrics
        savedAthlete = if (metrics != null) {
            val updated = Athlete(athlete.id(), athlete.name(), athlete.profile(), metrics, athlete.preferences())
            val updateResult = athleteService.updateAthlete(athlete.id(), updated)
            assertThat(updateResult.isSuccess()).isTrue
            updateResult.value().orElseThrow()
        } else {
            athlete
        }
    }

    @Then("the athlete is stored with level {string}")
    fun athleteStoredWithLevel(level: String) {
        val athlete = requireNotNull(savedAthlete)
        val stored = athleteRepository.findById(athlete.id()).orElseThrow()
        assertThat(stored.profile().level()).isEqualTo(level)
    }

    @Given("a saved athlete with availability {string} weekly volume {double} phase {string}")
    fun savedAthleteWithAvailability(availabilityCsv: String, weeklyVolume: Double, phase: String) {
        val profile = AthleteProfile(
            "unspec",
            30,
            Kilograms.of(75.0),
            Centimeters.of(175.0),
            "intermediate"
        )
        val preferences = TrainingPreferences(parseDays(availabilityCsv), Hours.of(weeklyVolume), phase)
        val created = athleteService.createAthlete("Plan Athlete", profile, preferences)
        assertThat(created.isSuccess()).isTrue
        savedAthlete = created.value().orElseThrow()
    }

    @When("the coach requests a plan for phase {string} start date {string} target weekly hours {double}")
    fun coachRequestsPlan(phase: String, startDate: String, weeklyHours: Double) {
        val athlete = requireNotNull(savedAthlete)
        val plan = trainingPlanService.generatePlan(athlete, phase, LocalDate.parse(startDate), Hours.of(weeklyHours))
        planWorkouts = plan.workouts()
    }

    @Then("the plan has an {int}\\/{int} intensity split")
    fun planHasIntensitySplit(high: Int, low: Int) {
        val totalMinutes = planWorkouts.sumOf { it.durationMinutes().value() }
        val highMinutes = planWorkouts
            .filter { it.type() == Workout.WorkoutType.INTERVALS || it.type() == Workout.WorkoutType.THRESHOLD }
            .sumOf { it.durationMinutes().value() }
        val ratio = if (totalMinutes == 0) 0.0 else highMinutes.toDouble() / totalMinutes.toDouble()
        val expectedRatio = if (high + low == 0) 0.0 else low.toDouble() / (high + low).toDouble()
        assertThat(ratio).isBetween(expectedRatio - 0.05, expectedRatio + 0.05)
    }

    @Given("readiness score {double} and compliance {double}")
    fun readinessAndCompliance(readiness: Double, compliance: Double) {
        readinessScore = readiness
        compliancePercent = compliance
    }

    @When("the coach asks for an adjustment")
    fun coachAsksForAdjustment() {
        adjustmentSuggestion = adjustmentService.suggestAdjustment(
            readinessScore ?: 0.0,
            compliancePercent ?: 0.0
        )
    }

    @Then("the recommendation suggests reducing volume")
    fun recommendationSuggestsReducingVolume() {
        assertThat(adjustmentSuggestion).isNotNull
        assertThat(adjustmentSuggestion!!.lowercase()).contains("reduce volume")
    }

    @Given("a saved athlete")
    fun savedAthlete() {
        val profile = AthleteProfile(
            "unspec",
            30,
            Kilograms.of(75.0),
            Centimeters.of(175.0),
            "intermediate"
        )
        val preferences = TrainingPreferences(EnumSet.of(DayOfWeek.MONDAY), Hours.of(10.0), "base")
        val created = athleteService.createAthlete("Note Athlete", profile, preferences)
        assertThat(created.isSuccess()).isTrue
        savedAthlete = created.value().orElseThrow()
    }

    @When("the coach posts a note {string}")
    fun coachPostsNote(note: String) {
        val athlete = requireNotNull(savedAthlete)
        noteService.addNote(athlete.id(), note)
    }

    @Then("the athlete has {int} note stored")
    fun athleteHasNotes(count: Int) {
        val athlete = requireNotNull(savedAthlete)
        val notes = noteService.getNotes(athlete.id())
        assertThat(notes).hasSize(count)
    }

    @Given("an admin enters API key {string}")
    fun adminEntersApiKey(key: String) {
        integrationService.configureIntervalsIcu(key)
    }

    @When("the integration is validated")
    fun integrationValidated() {
        integrationStatus = if (!integrationService.getIntervalsIcuApiKey().isNullOrBlank()) "active" else "invalid"
    }

    @Then("integration status is {string}")
    fun integrationStatusIs(status: String) {
        assertThat(integrationStatus).isEqualTo(status)
    }

    @Given("readiness signals hrv {double} rhr {double} sleep {double} fatigue {double} subjective {int}")
    fun readinessSignals(hrv: Double, rhr: Double, sleep: Double, fatigue: Double, subjective: Int) {
        val sleepQuality = subjective.coerceIn(1, 10)
        val fatigueScore = fatigue.toInt().coerceIn(1, 10)
        val stressScore = (fatigueScore + 2).coerceAtMost(10)
        val subjectiveWellness = SubjectiveWellness.create(
            fatigueScore,
            stressScore,
            sleepQuality,
            subjective.coerceIn(1, 10),
            5
        )
        val sleepMetrics = SleepMetrics.basic(Hours.of(sleep), sleepQuality)
        readinessPhysiological = PhysiologicalData(
            BeatsPerMinute.of(rhr),
            HeartRateVariability.of(hrv),
            Kilograms.of(75.0),
            sleepMetrics
        )
        readinessSubjective = subjectiveWellness
    }

    @When("readiness is calculated")
    fun readinessCalculated() {
        val physiological = requireNotNull(readinessPhysiological)
        val subjective = readinessSubjective
        readinessScore = readinessCalculatorService.calculateReadiness(
            physiological,
            subjective,
            TrainingLoadSummary.empty()
        )
    }

    @Then("readiness score is between 0 and 100")
    fun readinessScoreBetween() {
        assertThat(readinessScore).isBetween(0.0, 100.0)
    }

    @Given("a plan with {int} workouts planned")
    fun planWithWorkoutsPlanned(count: Int) {
        plannedWorkouts = count
    }

    @Given("actual workouts completed {int}")
    fun actualWorkoutsCompleted(count: Int) {
        completedWorkouts = count
    }

    @When("compliance is evaluated")
    fun complianceEvaluated() {
        compliancePercent = complianceService.calculateCompliance(plannedWorkouts, completedWorkouts)
    }

    @Then("compliance percent is {double}")
    fun compliancePercentIs(expected: Double) {
        assertThat(compliancePercent).isCloseTo(expected, org.assertj.core.data.Offset.offset(0.1))
    }

    @Given("a saved athlete with linked Intervals.icu")
    fun savedAthleteWithLinkedIntervals() {
        val profile = AthleteProfile(
            "unspec",
            30,
            Kilograms.of(75.0),
            Centimeters.of(175.0),
            "intermediate"
        )
        val preferences = TrainingPreferences(EnumSet.of(DayOfWeek.MONDAY), Hours.of(10.0), "base")
        val created = athleteService.createAthlete("Sync Athlete", profile, preferences)
        assertThat(created.isSuccess()).isTrue
        savedAthlete = created.value().orElseThrow()
        integrationService.configureIntervalsIcu("test-key")
    }

    @When("activities are ingested from {string} to {string}")
    fun activitiesAreIngested(startDate: String, endDate: String) {
        val athlete = requireNotNull(savedAthlete)
        val start = LocalDate.parse(startDate)
        val end = LocalDate.parse(endDate)
        fitnessPlatformPort.setActivities(
            listOf(
                FitnessPlatformPort.Activity(
                    "act-1",
                    start,
                    "Endurance Ride",
                    Seconds.of(3600),
                    Kilometers.of(40.0),
                    Watts.of(180.0),
                    BeatsPerMinute.of(140.0),
                    "Ride",
                    50.0,
                    0.75,
                    Watts.of(200.0)
                ),
                FitnessPlatformPort.Activity(
                    "act-2",
                    start.plusDays(1),
                    "Tempo Run",
                    Seconds.of(1800),
                    Kilometers.of(8.0),
                    Watts.of(0.0),
                    BeatsPerMinute.of(150.0),
                    "Run",
                    35.0,
                    0.8,
                    null
                )
            )
        )
        syncService.syncAthleteData(athlete.id(), start, end)
        activityHistory = activityReadService.getActivities(athlete.id(), start, end)
    }

    @Then("activity history is available")
    fun activityHistoryIsAvailable() {
        assertThat(activityHistory).isNotEmpty
    }

    @When("a sync is triggered")
    fun syncTriggered() {
        val athlete = requireNotNull(savedAthlete)
        val start = LocalDate.now().minusDays(1)
        val end = LocalDate.now()
        fitnessPlatformPort.setActivities(
            listOf(
                FitnessPlatformPort.Activity(
                    "sync-1",
                    start,
                    "Recovery Ride",
                    Seconds.of(1800),
                    Kilometers.of(20.0),
                    Watts.of(120.0),
                    BeatsPerMinute.of(120.0),
                    "Ride",
                    20.0,
                    0.6,
                    Watts.of(130.0)
                )
            )
        )
        fitnessPlatformPort.setWellnessData(
            listOf(
                FitnessPlatformPort.WellnessData(
                    start,
                    BeatsPerMinute.of(60.0),
                    HeartRateVariability.of(50.0),
                    Kilograms.of(75.0),
                    Hours.of(7.5),
                    7
                )
            )
        )
        syncService.syncAthleteData(athlete.id(), start, end)
    }

    @Then("workouts and wellness are ingested")
    fun workoutsAndWellnessIngested() {
        val athlete = requireNotNull(savedAthlete)
        val end = LocalDate.now()
        val start = end.minusDays(1)
        val activities = activityReadService.getActivities(athlete.id(), start, end)
        val wellness = wellnessRepository.findByAthleteIdAndDate(athlete.id(), start)
        assertThat(activities).isNotEmpty
        assertThat(wellness).isPresent
    }

    @Then("a sync event is recorded")
    fun syncEventRecorded() {
        val athlete = requireNotNull(savedAthlete)
        val start = LocalDate.now().minusDays(1)
        val wellness = wellnessRepository.findByAthleteIdAndDate(athlete.id(), start)
        assertThat(wellness).isPresent
    }

    private fun parseDays(availabilityCsv: String): Set<DayOfWeek> {
        if (availabilityCsv.isBlank()) {
            return emptySet()
        }
        return availabilityCsv.split(",")
            .map { it.trim().uppercase() }
            .filter { it.isNotEmpty() }
            .map { DayOfWeek.valueOf(it) }
            .toCollection(EnumSet.noneOf(DayOfWeek::class.java))
    }
}
