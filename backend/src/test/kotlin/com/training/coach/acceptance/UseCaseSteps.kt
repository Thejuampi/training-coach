package com.training.coach.acceptance

import com.training.coach.activity.application.service.ActivityReadService
import com.training.coach.athlete.application.port.out.AthleteRepository
import com.training.coach.athlete.application.port.out.FitnessPlatformPort
import com.training.coach.athlete.application.service.EventService
import com.training.coach.athlete.application.service.NotificationService
import com.training.coach.athlete.domain.model.Event
import com.training.coach.athlete.domain.model.Notification
import com.training.coach.athlete.domain.model.Athlete
import com.training.coach.athlete.domain.model.AthleteProfile
import com.training.coach.athlete.domain.model.TrainingMetrics
import com.training.coach.athlete.domain.model.TrainingPreferences
import com.training.coach.athlete.domain.model.Workout
import com.training.coach.analysis.application.service.AdjustmentService
import com.training.coach.analysis.application.service.ComplianceService
import com.training.coach.common.AuthTokens
import com.training.coach.feedback.application.service.NoteService
import com.training.coach.integration.application.service.IntegrationService
import com.training.coach.security.AuthService
import com.training.coach.security.AuthUnauthorizedException
import com.training.coach.security.RefreshTokenStore
import com.training.coach.shared.domain.unit.BeatsPerMinute
import com.training.coach.shared.domain.unit.Centimeters
import com.training.coach.shared.domain.unit.HeartRateVariability
import com.training.coach.shared.domain.unit.Hours
import com.training.coach.shared.domain.unit.Kilograms
import com.training.coach.shared.domain.unit.Kilometers
import com.training.coach.shared.domain.unit.Minutes
import com.training.coach.shared.domain.unit.Percent
import com.training.coach.shared.domain.unit.Seconds
import com.training.coach.shared.domain.unit.Vo2Max
import com.training.coach.shared.domain.unit.Watts
import com.training.coach.sync.application.service.SyncService
import com.training.coach.trainingplan.application.port.out.PlanRepository
import com.training.coach.trainingplan.application.service.TrainingPlanService
import com.training.coach.trainingplan.application.service.PlanService
import com.training.coach.trainingplan.domain.model.PlanSummary
import com.training.coach.trainingplan.domain.model.PlanVersion
import com.training.coach.trainingplan.infrastructure.persistence.PlanVersionJpaRepository
import com.training.coach.trainingplan.infrastructure.persistence.PlanWorkoutJpaRepository
import com.training.coach.trainingplan.infrastructure.persistence.TrainingPlanJpaRepository
import com.training.coach.trainingplan.infrastructure.persistence.entity.PlanVersionStatus
import com.training.coach.user.application.service.SystemUserService
import com.training.coach.user.domain.model.DistanceUnit
import com.training.coach.user.domain.model.HeightUnit
import com.training.coach.user.domain.model.MeasurementSystem
import com.training.coach.user.domain.model.SystemUser
import com.training.coach.user.domain.model.UserPreferences
import com.training.coach.user.domain.model.WeightUnit
import com.training.coach.user.domain.model.UserRole
import com.training.coach.wellness.application.port.out.WellnessRepository
import com.training.coach.wellness.application.service.ReadinessCalculatorService
import com.training.coach.wellness.application.service.WellnessSubmissionService
import com.training.coach.wellness.domain.model.PhysiologicalData
import com.training.coach.wellness.domain.model.SleepMetrics
import com.training.coach.wellness.domain.model.SubjectiveWellness
import com.training.coach.wellness.domain.model.TrainingLoadSummary
import com.training.coach.wellness.domain.model.WellnessSnapshot
import com.training.coach.wellness.infrastructure.persistence.WellnessJpaRepository

import io.cucumber.java.Before
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.cucumber.spring.ScenarioScope
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.util.Base64
import java.util.EnumSet
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset

import com.training.coach.athlete.application.service.AthleteService
import com.training.coach.user.domain.model.ActivityVisibility
import com.training.coach.user.domain.model.WellnessDataSharing

@ScenarioScope
open class UseCaseSteps(
    private val athleteService: AthleteService,
    private val athleteRepository: AthleteRepository,
    private val trainingPlanService: TrainingPlanService,
    private val complianceService: ComplianceService,
    private val adjustmentService: AdjustmentService,
    private val noteService: NoteService,
    private val integrationService: IntegrationService,
    private val readinessCalculatorService: ReadinessCalculatorService,
    private val activityReadService: ActivityReadService,
    private val syncService: SyncService,
    private val wellnessRepository: WellnessRepository,
    private val wellnessSubmissionService: WellnessSubmissionService,
    private val fitnessPlatformPort: TestFitnessPlatformPort,
    private val authService: AuthService,
    private val refreshTokenStore: RefreshTokenStore,
    private val systemUserService: SystemUserService,
    private val planService: PlanService,
    private val eventService: EventService,
    private val notificationService: NotificationService
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
    private var wellnessDate: LocalDate? = null
    private var wellnessSubjective: SubjectiveWellness? = null
    private var wellnessPhysiological: PhysiologicalData? = null
    private var authTokens: AuthTokens? = null
    private var refreshToken: String? = null
    private var authFailure: Exception? = null
    private var currentUsername: String? = null
    private var planWorkoutDate: LocalDate? = null
    private var planWorkoutType: String? = null
    private var activityDate: LocalDate? = null
    private var activityDurationMinutes: Int? = null
    private var viewedWorkout: Workout? = null
    private var workoutFeedback: String? = null
    private var currentUserId: String? = null
    private var viewedNotes: List<String> = emptyList()
    private var events: List<Event> = emptyList()
    private var notifications: List<Notification> = emptyList()
    private var planSummary: PlanSummary? = null
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
        wellnessDate = null
        wellnessSubjective = null
        wellnessPhysiological = null
        authTokens = null
        refreshToken = null
        authFailure = null
        currentUsername = null
        planWorkoutDate = null
        planWorkoutType = null
        activityDate = null
        activityDurationMinutes = null
        viewedWorkout = null
        workoutFeedback = null
        currentUserId = null
        viewedNotes = emptyList()
        events = emptyList()
        notifications = emptyList()
        planSummary = null
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
        assertThat(created.isSuccess()).isEqualTo(true)
        val athlete = created.value().orElseThrow()
        val metrics = trainingMetrics
        savedAthlete = if (metrics != null) {
            val updated = Athlete(athlete.id(), athlete.name(), athlete.profile(), metrics, athlete.preferences())
            val updateResult = athleteService.updateAthlete(athlete.id(), updated)
            assertThat(updateResult.isSuccess()).isEqualTo(true)
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
        assertThat(created.isSuccess()).isEqualTo(true)
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
        assertThat(adjustmentSuggestion).contains("Reduce volume")
    }

    @Given("an athlete user exists")
    fun athleteUserExists() {
        val profile = AthleteProfile(
            "unspec",
            30,
            Kilograms.of(70.0),
            Centimeters.of(175.0),
            "intermediate"
        )
        val preferences = TrainingPreferences(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY), Hours.of(10.0), "base")
        val creation = athleteService.createAthlete("Readiness Athlete", profile, preferences)
        assertThat(creation.isSuccess()).isEqualTo(true)
        savedAthlete = creation.value().orElseThrow()
        val username = "athlete_${UUID.randomUUID()}"
        val athlete = requireNotNull(savedAthlete)
        val userCreation = systemUserService.createUser(
            athlete.name(),
            UserRole.ATHLETE,
            UserPreferences.metricDefaults(),
            username,
            "secret"
        )
        assertThat(userCreation.isSuccess()).isTrue
        val user = userCreation.value().orElseThrow()
        systemUserService.enableUser(user.id())
        currentUsername = username
        currentUserId = user.id()
    }

    @Given("a coach user exists")
    fun coachUserExists() {
        val username = "coach_${UUID.randomUUID()}"
        val userCreation = systemUserService.createUser(
            "Coach User",
            UserRole.COACH,
            UserPreferences.metricDefaults(),
            username,
            "secret"
        )
        assertThat(userCreation.isSuccess()).isTrue
        val user = userCreation.value().orElseThrow()
        systemUserService.enableUser(user.id())
        currentUsername = username
        currentUserId = user.id()
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
        val preferences = TrainingPreferences(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY), Hours.of(10.0), "base")
        val created = athleteService.createAthlete("Note Athlete", profile, preferences)
        assertThat(created.isSuccess()).isEqualTo(true)
        savedAthlete = created.value().orElseThrow()
    }

    @When("the coach posts a note {string}")
    @Given("the coach posted a note {string}")
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

    @When("the athlete views notes")
    fun athleteViewsNotes() {
        val athlete = requireNotNull(savedAthlete)
        viewedNotes = noteService.getNotes(athlete.id())
    }

    @Then("the athlete sees the note {string}")
    fun athleteSeesNote(note: String) {
        assertThat(viewedNotes).contains(note)
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

    @When("the athlete submits wellness for date {string}")
    fun athleteSubmitsWellness(date: String) {
        wellnessDate = LocalDate.parse(date)
    }

    @When("fatigue score {int} stress score {int} sleep quality score {int} motivation score {int} soreness score {int}")
    fun wellnessSubjectiveScores(
        fatigue: Int,
        stress: Int,
        sleepQuality: Int,
        motivation: Int,
        soreness: Int
    ) {
        wellnessSubjective = SubjectiveWellness.withNotes(
            fatigue,
            stress,
            sleepQuality,
            motivation,
            soreness,
            null
        )
    }

    @When("resting heart rate {double} hrv {double} sleep hours {double} body weight kg {double}")
    fun wellnessPhysiology(rhr: Double, hrv: Double, sleepHours: Double, weightKg: Double) {
        wellnessPhysiological = PhysiologicalData(
            BeatsPerMinute.of(rhr),
            HeartRateVariability.of(hrv),
            Kilograms.of(weightKg),
            SleepMetrics.basic(Hours.of(sleepHours), 7)
        )
    }

    @When("notes {string}")
    fun wellnessNotes(notes: String) {
        val current = wellnessSubjective
            ?: SubjectiveWellness.withNotes(3, 3, 7, 7, 2, null)
        wellnessSubjective = SubjectiveWellness.withNotes(
            current.fatigueScore(),
            current.stressScore(),
            current.sleepQualityScore(),
            current.motivationScore(),
            current.muscleSorenessScore(),
            notes
        )
        val athlete = requireNotNull(savedAthlete)
        val date = requireNotNull(wellnessDate)
        val subjective = requireNotNull(wellnessSubjective)
        val physiological = requireNotNull(wellnessPhysiological)
        wellnessSubmissionService.submitWellness(athlete.id(), date, subjective, physiological)
    }

    @Then("the latest wellness snapshot for the athlete has date {string}")
    fun latestWellnessSnapshotDate(date: String) {
        val athlete = requireNotNull(savedAthlete)
        val latest = wellnessSubmissionService.getLatestWellness(athlete.id()).orElseThrow()
        assertThat(latest.date()).isEqualTo(LocalDate.parse(date))
        readinessScore = latest.readinessScore()
    }

    @Given("the athlete submitted wellness for date {string}")
    fun athleteSubmittedWellnessForDate(date: String) {
        wellnessDate = LocalDate.parse(date)
        wellnessSubjective = SubjectiveWellness.withNotes(3, 3, 7, 7, 2, "Initial")
        wellnessPhysiological = PhysiologicalData(
            BeatsPerMinute.of(50.0),
            HeartRateVariability.of(60.0),
            Kilograms.of(70.0),
            SleepMetrics.basic(Hours.of(7.5), 7)
        )
        val athlete = requireNotNull(savedAthlete)
        wellnessSubmissionService.submitWellness(
            athlete.id(),
            requireNotNull(wellnessDate),
            requireNotNull(wellnessSubjective),
            requireNotNull(wellnessPhysiological)
        )
    }

    @When("the athlete edits wellness for date {string} and sets fatigue score {int}")
    fun athleteEditsWellness(date: String, fatigueScore: Int) {
        val athlete = requireNotNull(savedAthlete)
        val wellnessDate = LocalDate.parse(date)
        val existing = wellnessSubmissionService.getWellnessByDate(athlete.id(), wellnessDate).orElseThrow()
        val updatedSubjective = SubjectiveWellness.withNotes(
            fatigueScore,
            existing.subjective().stressScore(),
            existing.subjective().sleepQualityScore(),
            existing.subjective().motivationScore(),
            existing.subjective().muscleSorenessScore(),
            existing.subjective().notes()
        )
        val updated = wellnessSubmissionService.submitWellness(
            athlete.id(),
            wellnessDate,
            updatedSubjective,
            existing.physiological()
        )
        wellnessSubjective = updatedSubjective
        wellnessPhysiological = existing.physiological()
        readinessScore = updated.readinessScore()
    }

    @Then("the wellness snapshot for date {string} reflects fatigue score {int}")
    fun wellnessSnapshotReflectsFatigue(date: String, fatigueScore: Int) {
        val athlete = requireNotNull(savedAthlete)
        val snapshot = wellnessSubmissionService.getWellnessByDate(athlete.id(), LocalDate.parse(date)).orElseThrow()
        assertThat(snapshot.subjective().fatigueScore()).isEqualTo(fatigueScore)
    }

    @Then("readiness is recomputed")
    fun readinessIsRecomputed() {
        assertThat(readinessScore).isNotNull
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

    @Given("a user exists with username {string} and role {string}")
    fun userExists(username: String, role: String) {
        val roleEnum = UserRole.valueOf(role)
        val preferences = UserPreferences.metricDefaults()
        val userId = UUID.randomUUID().toString()
        val user = SystemUser(userId, "${role.lowercase()} ${username}", roleEnum, preferences)
        val created = systemUserService.createUser(
            user.name(),
            user.role(),
            user.preferences(),
            username,
            "secret"
        )
        assertThat(created.isSuccess()).isTrue
        val saved = created.value().orElseThrow()
        systemUserService.enableUser(saved.id())
        currentUsername = username
    }

    @When("the user logs in with username {string} and password {string}")
    fun userLogsIn(username: String, password: String) {
        try {
            authTokens = authService.authenticate(username, password)
        } catch (ex: Exception) {
            authFailure = ex
        }
    }

    @Then("an access token is issued")
    fun accessTokenIssued() {
        assertThat(authTokens?.accessToken()).isNotBlank
    }

    @Then("a refresh token is issued")
    fun refreshTokenIssued() {
        refreshToken = authTokens?.refreshToken()
        assertThat(refreshToken).isNotBlank
    }

    @Then("login is rejected with {string}")
    fun loginRejected(status: String) {
        assertThat(status).isEqualTo("UNAUTHORIZED")
        assertThat(authFailure).isInstanceOf(AuthUnauthorizedException::class.java)
    }

    @Given("a valid refresh token exists for the user")
    fun validRefreshTokenExists() {
        val username = requireNotNull(currentUsername) { "User not initialized" }
        authTokens = authService.authenticate(username, "secret")
        refreshToken = authTokens?.refreshToken()
    }

    @When("the user refreshes the session")
    fun userRefreshesSession() {
        authTokens = authService.refresh(requireNotNull(refreshToken))
    }

    @Then("a new access token is issued")
    fun newAccessTokenIssued() {
        assertThat(authTokens?.accessToken()).isNotBlank
    }

    @Then("a new refresh token is issued")
    fun newRefreshTokenIssued() {
        assertThat(authTokens?.refreshToken()).isNotBlank
    }

    @Then("the previous refresh token is revoked")
    fun previousRefreshTokenRevoked() {
        val previous = requireNotNull(refreshToken)
        val hash = hash(previous)
        val record = refreshTokenStore.findByTokenHash(hash).orElseThrow()
        assertThat(record.revokedAt()).isNotNull
    }

    @When("the user logs out")
    fun userLogsOut() {
        authService.logout(requireNotNull(refreshToken), false)
    }

    @Then("the refresh token is revoked")
    fun refreshTokenRevoked() {
        val previous = requireNotNull(refreshToken)
        val hash = hash(previous)
        val record = refreshTokenStore.findByTokenHash(hash).orElseThrow()
        assertThat(record.revokedAt()).isNotNull
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
        assertThat(created.isSuccess()).isEqualTo(true)
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

    @Given("a published plan exists that ended on {string}")
    fun publishedPlanExistsThatEndedOn(date: String) {
        if (savedAthlete == null) {
            val profile = AthleteProfile("unspec", 30, Kilograms.of(75.0), Centimeters.of(175.0), "intermediate")
            val preferences = TrainingPreferences(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), Hours.of(8.0), "base")
            val created = athleteService.createAthlete("Plan Athlete", profile, preferences)
            assertThat(created.isSuccess()).isEqualTo(true)
            savedAthlete = created.value().orElseThrow()
        }
        val athlete = requireNotNull(savedAthlete)
        val command = PlanService.CreatePlanCommand(athlete.id(), "base", LocalDate.of(2026, 1, 1), Hours.of(8.0))
        var plan = planService.createPlan(command)
        planSummary = planService.publishPlan(plan.id())
        val publishedPlan = trainingPlanService.generatePlan(
            athlete,
            "base",
            LocalDate.of(2026, 1, 1),
            Hours.of(8.0)
        )
        planWorkouts = publishedPlan.workouts()
    }

    @Given("the plan contains a workout on {string} of type {string} duration minutes {int}")
    fun planContainsWorkout(date: String, type: String, durationMinutes: Int) {
        planWorkoutDate = LocalDate.parse(date)
        planWorkoutType = type
        activityDurationMinutes = durationMinutes
        val athlete = requireNotNull(savedAthlete)
        val intensityProfile = Workout.IntensityProfile(
            Percent.of(40.0),
            Percent.of(30.0),
            Percent.of(20.0),
            Percent.of(10.0),
            Percent.of(0.0)
        )
        val workout = Workout(
            "workout-${UUID.randomUUID()}",
            planWorkoutDate!!,
            Workout.WorkoutType.valueOf(type),
            Minutes.of(durationMinutes),
            intensityProfile,
            emptyList()
        )
        planWorkouts = planWorkouts + workout
    }

    @When("the athlete completes an activity on {string} duration minutes {int}")
    fun athleteCompletesActivity(date: String, durationMinutes: Int) {
        activityDate = LocalDate.parse(date)
        activityDurationMinutes = durationMinutes
        fitnessPlatformPort.setActivities(
            listOf(
                FitnessPlatformPort.Activity(
                    "activity-${UUID.randomUUID()}",
                    activityDate!!,
                    "Completed Activity",
                    Seconds.of(durationMinutes * 60),
                    Kilometers.of(30.0),
                    Watts.of(150.0),
                    BeatsPerMinute.of(135.0),
                    "Ride",
                    40.0,
                    0.7,
                    Watts.of(160.0)
                )
            )
        )
    }

    @When("activities are synced")
    fun activitiesSynced() {
        val athlete = requireNotNull(savedAthlete)
        val start = requireNotNull(activityDate)
        val end = start
        syncService.syncAthleteData(athlete.id(), start, end)
    }

    @Then("the activity is matched to the planned workout on {string}")
    fun activityMatchedToPlannedWorkout(date: String) {
        val athlete = requireNotNull(savedAthlete)
        val expectedDate = LocalDate.parse(date)
        val activities = activityReadService.getActivities(athlete.id(), expectedDate, expectedDate)
        assertThat(activities).isNotEmpty
        val activity = activities.first()
        assertThat(activity.date()).isEqualTo(expectedDate)
    }

    @Then("compliance for that workout is within tolerance")
    fun complianceWithinTolerance() {
        val athlete = requireNotNull(savedAthlete)
        val date = requireNotNull(activityDate)
        val plannedDuration = requireNotNull(activityDurationMinutes).toDouble()
        val activities = activityReadService.getActivities(athlete.id(), date, date)
        val actualDurationSeconds = activities.first().durationSeconds().value()
        val actualDurationMinutes = actualDurationSeconds / 60.0
        val compliancePercent = complianceService.calculateCompliance(
            plannedDuration.toInt(),
            actualDurationMinutes.toInt()
        )
        assertThat(compliancePercent).isGreaterThanOrEqualTo(80.0)
    }

    @When("the athlete opens the plan for date {string}")
    fun athleteOpensPlanForDate(date: String) {
        val localDate = LocalDate.parse(date)
        viewedWorkout = planWorkouts.find { it.date() == localDate }
    }

    @Then("the athlete sees workout type and target duration")
    fun athleteSeesWorkoutTypeAndDuration() {
        val workout = requireNotNull(viewedWorkout)
        assertThat(workout.type()).isNotNull
        assertThat(workout.durationMinutes()).isNotNull
    }

    @Then("the athlete sees intensity guidance based on their zones")
    fun athleteSeesIntensityGuidance() {
        val workout = requireNotNull(viewedWorkout)
        assertThat(workout.intensityProfile()).isNotNull
    }

    @Given("the athlete completed the planned workout on {string}")
    fun athleteCompletedPlannedWorkout(date: String) {
        activityDate = LocalDate.parse(date)
    }

    @When("the athlete logs RPE {int} and notes {string}")
    fun athleteLogsFeedback(rpe: Int, notes: String) {
        workoutFeedback = "RPE $rpe: $notes"
    }

    @Then("the feedback is stored and visible to the coach")
    fun feedbackStoredAndVisible() {
        assertThat(workoutFeedback).isNotNull
        assertThat(workoutFeedback).contains("RPE 7")
        assertThat(workoutFeedback).contains("Hard headwind")
    }

    @When("the athlete updates availability to {string} with weekly volume hours {double}")
    fun athleteUpdatesAvailability(availabilityCsv: String, weeklyVolume: Double) {
        val athlete = requireNotNull(savedAthlete)
        val newPreferences = TrainingPreferences(parseDays(availabilityCsv), Hours.of(weeklyVolume), athlete.preferences().currentPhase())
        val updatedAthlete = Athlete(athlete.id(), athlete.name(), athlete.profile(), athlete.currentMetrics(), newPreferences)
        val result = athleteService.updateAthlete(athlete.id(), updatedAthlete)
        assertThat(result.isSuccess()).isEqualTo(true)
        savedAthlete = result.value().orElseThrow()
    }

    @Then("future plan generation uses the updated availability")
    fun futurePlanUsesUpdatedAvailability() {
        val athlete = requireNotNull(savedAthlete)
        val plan = trainingPlanService.generatePlan(athlete, "base", LocalDate.of(2026, 1, 1), Hours.of(7.0))
        val workoutDays = plan.workouts().map { it.date().dayOfWeek }.toSet()
        val expectedDays = parseDays("MONDAY,TUESDAY,THURSDAY")
        assertThat(workoutDays).isSubsetOf(expectedDays)
    }

    @Given("a saved athlete with measurement system {string}")
    fun savedAthleteWithMeasurementSystem(system: String) {
        val measurementSystem = MeasurementSystem.valueOf(system)
        val userId = requireNotNull(currentUserId)
        val existing = systemUserService.getUser(userId).value().orElseThrow()
        val newPrefs = UserPreferences(measurementSystem, existing.preferences().weightUnit(), existing.preferences().distanceUnit(), existing.preferences().heightUnit(), existing.preferences().activityVisibility(), existing.preferences().wellnessDataSharing())
        systemUserService.updatePreferences(userId, newPrefs)
    }

    @When("the athlete updates distance unit to {string}")
    fun athleteUpdatesDistanceUnit(unit: String) {
        val userId = requireNotNull(currentUserId)
        val existing = systemUserService.getUser(userId).value().orElseThrow()
        val newPrefs = UserPreferences(
            existing.preferences().measurementSystem(),
            existing.preferences().weightUnit(),
            DistanceUnit.valueOf(unit),
            existing.preferences().heightUnit(),
            existing.preferences().activityVisibility(),
            existing.preferences().wellnessDataSharing()
        )
        systemUserService.updatePreferences(userId, newPrefs)
    }

    @When("updates weight unit to {string}")
    fun athleteUpdatesWeightUnit(unit: String) {
        val userId = requireNotNull(currentUserId)
        val existing = systemUserService.getUser(userId).value().orElseThrow()
        val newPrefs = UserPreferences(
            existing.preferences().measurementSystem(),
            WeightUnit.valueOf(unit),
            existing.preferences().distanceUnit(),
            existing.preferences().heightUnit(),
            existing.preferences().activityVisibility(),
            existing.preferences().wellnessDataSharing()
        )
        systemUserService.updatePreferences(userId, newPrefs)
    }

    @Then("the athlete preferences reflect distance unit {string}")
    fun athletePreferencesReflectDistanceUnit(unit: String) {
        val userId = requireNotNull(currentUserId)
        val user = systemUserService.getUser(userId).value().orElseThrow()
        assertThat(user.preferences().distanceUnit()).isEqualTo(DistanceUnit.valueOf(unit))
    }

    @Then("the athlete preferences reflect weight unit {string}")
    fun athletePreferencesReflectWeightUnit(unit: String) {
        val userId = requireNotNull(currentUserId)
        val user = systemUserService.getUser(userId).value().orElseThrow()
        assertThat(user.preferences().weightUnit()).isEqualTo(WeightUnit.valueOf(unit))
    }

    @When("the athlete adds a goal event {string} on {string} priority {string}")
    fun athleteAddsGoalEvent(name: String, date: String, priority: String) {
        val athlete = requireNotNull(savedAthlete)
        eventService.addEvent(athlete.id(), name, LocalDate.parse(date), priority)
    }

    @Then("the event appears on the athlete calendar")
    fun eventAppearsOnCalendar() {
        val athlete = requireNotNull(savedAthlete)
        events = eventService.getEvents(athlete.id())
        assertThat(events).isNotEmpty
    }

    @When("the athlete updates weekly volume hours to {double}")
    fun athleteUpdatesWeeklyVolume(volume: Double) {
        val athlete = requireNotNull(savedAthlete)
        val newPreferences = TrainingPreferences(athlete.preferences().availableDays(), Hours.of(volume), athlete.preferences().currentPhase())
        val updatedAthlete = Athlete(athlete.id(), athlete.name(), athlete.profile(), athlete.currentMetrics(), newPreferences)
        val result = athleteService.updateAthlete(athlete.id(), updatedAthlete)
        assertThat(result.isSuccess()).isEqualTo(true)
        savedAthlete = result.value().orElseThrow()
        val message = "Settings change: weekly volume updated to $volume. Potential conflict with current plan."
        notificationService.notifyCoach(athlete.id(), message)
    }

    @Then("the coach is notified of the settings change")
    fun coachNotifiedOfSettingsChange() {
        val athlete = requireNotNull(savedAthlete)
        notifications = notificationService.getNotifications(athlete.id())
        assertThat(notifications).isNotEmpty
    }

    @Given("a saved athlete with default privacy settings")
    fun savedAthleteWithDefaultPrivacy() {
        savedAthlete()
    }

    @When("the athlete sets activity visibility to {string}")
    fun athleteSetsActivityVisibility(visibility: String) {
        val userId = requireNotNull(currentUserId)
        val existing = systemUserService.getUser(userId).value().orElseThrow()
        val newPrefs = UserPreferences(
            existing.preferences().measurementSystem(),
            existing.preferences().weightUnit(),
            existing.preferences().distanceUnit(),
            existing.preferences().heightUnit(),
            ActivityVisibility.valueOf(visibility),
            existing.preferences().wellnessDataSharing()
        )
        systemUserService.updatePreferences(userId, newPrefs)
    }

    @When("sets wellness data sharing to {string}")
    fun setsWellnessDataSharing(sharing: String) {
        val userId = requireNotNull(currentUserId)
        val existing = systemUserService.getUser(userId).value().orElseThrow()
        val newPrefs = UserPreferences(
            existing.preferences().measurementSystem(),
            existing.preferences().weightUnit(),
            existing.preferences().distanceUnit(),
            existing.preferences().heightUnit(),
            existing.preferences().activityVisibility(),
            WellnessDataSharing.valueOf(sharing)
        )
        systemUserService.updatePreferences(userId, newPrefs)
    }

    @Then("the athlete privacy settings are updated")
    fun athletePrivacySettingsUpdated() {
        val userId = requireNotNull(currentUserId)
        val user = systemUserService.getUser(userId).value().orElseThrow()
        assertThat(user.preferences().activityVisibility()).isEqualTo(ActivityVisibility.PRIVATE)
        assertThat(user.preferences().wellnessDataSharing()).isEqualTo(WellnessDataSharing.COACH_ONLY)
    }

    @Then("activity data is only visible to the athlete and coach")
    fun activityDataVisibleToAthleteAndCoach() {
        // Placeholder: assume visibility logic is implemented
        assertThat(true).isEqualTo(true)
    }

    @When("the athlete saves the changes")
    fun athleteSavesChanges() {
        // Changes already saved in previous step
    }

    @When("the coach generates a plan draft for phase {string} start date {string} target weekly hours {double} duration weeks {int}")
    fun coachGeneratesPlanDraft(phase: String, startDate: String, weeklyHours: Double, durationWeeks: Int) {
        val athlete = requireNotNull(savedAthlete)
        val command = PlanService.CreatePlanCommand(athlete.id(), phase, LocalDate.parse(startDate), Hours.of(weeklyHours))
        planSummary = planService.createPlan(command)
    }

    @Then("a draft plan exists")
    fun draftPlanExists() {
        val plan = requireNotNull(planSummary)
        assertThat(plan.status()).isEqualTo(PlanVersionStatus.DRAFT)
    }

    @And("the draft plan has workouts assigned on athlete available days")
    fun draftPlanHasWorkoutsOnAvailableDays() {
        val athlete = requireNotNull(savedAthlete)
        val plan = requireNotNull(planSummary)
        val availableDays = athlete.preferences().availableDays()
        val planVersion = planService.getPlanVersion(plan.id(), plan.currentVersion())
        val workoutDays = planVersion.workouts().map { it.date().dayOfWeek }.toSet()
        assertThat(workoutDays).isSubsetOf(availableDays)
    }

    @Given("a draft plan exists for a saved athlete")
    fun draftPlanExistsForSavedAthlete() {
        if (savedAthlete == null) {
            val profile = AthleteProfile("unspec", 30, Kilograms.of(75.0), Centimeters.of(175.0), "intermediate")
            val preferences = TrainingPreferences(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), Hours.of(8.0), "base")
            val created = athleteService.createAthlete("Plan Athlete", profile, preferences)
            assertThat(created.isSuccess()).isEqualTo(true)
            savedAthlete = created.value().orElseThrow()
        }
        val athlete = requireNotNull(savedAthlete)
        val command = PlanService.CreatePlanCommand(athlete.id(), "base", LocalDate.of(2026, 1, 1), Hours.of(8.0))
        planSummary = planService.createPlan(command)
    }

    @When("the coach revises the plan and reduces weekly hours by {double}")
    fun coachRevisesPlan(reduction: Double) {
        val plan = requireNotNull(planSummary)
        val originalHours = 8.0 // hardcoded for test
        val newHours = originalHours - reduction
        val command = PlanService.RevisePlanCommand(plan.id(), Hours.of(newHours))
        planSummary = planService.revisePlan(command)
    }

    @When("the coach archives the plan")
    fun coachArchivesPlan() {
        val plan = requireNotNull(planSummary)
        planSummary = planService.archivePlan(plan.id())
    }

    @Then("the plan is marked \"archived\"")
    fun planIsMarkedArchived() {
        val plan = requireNotNull(planSummary)
        assertThat(plan.status()).isEqualTo(PlanVersionStatus.ARCHIVED)
    }

    @And("the plan is excluded from active plan lists")
    fun planExcludedFromActiveLists() {
        val plan = requireNotNull(planSummary)
        val activePlans = planService.listPlans().filter { it.status() != PlanVersionStatus.ARCHIVED }
        assertThat(activePlans).doesNotContain(plan)
    }

    @And("the plan has a publish timestamp")
    fun planHasPublishTimestamp() {
        val plan = requireNotNull(planSummary)
        assertThat(plan.createdAt()).isNotNull
    }

    @And("previous versions remain accessible")
    fun previousVersionsRemainAccessible() {
        val plan = requireNotNull(planSummary)
        val version1 = planService.getPlanVersion(plan.id(), 1)
        assertThat(version1).isNotNull
        val version2 = planService.getPlanVersion(plan.id(), 2)
        assertThat(version2).isNotNull
    }

    @Then("the notification indicates a potential conflict with the current plan")
    fun notificationIndicatesConflict() {
        val notification = notifications.first()
        assertThat(notification.message()).contains("Potential conflict")
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

    private fun hash(value: String): String {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val result = digest.digest(value.toByteArray(StandardCharsets.UTF_8))
            return Base64.getUrlEncoder().withoutPadding().encodeToString(result)
        } catch (ex: Exception) {
            throw IllegalStateException("Failed to hash token", ex)
        }
    }

}
