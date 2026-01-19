package com.training.coach.acceptance

import com.training.coach.activity.application.port.out.ActivityRepository
import com.training.coach.activity.domain.model.ActivityLight
import com.training.coach.athlete.application.port.out.AthleteRepository
import com.training.coach.athlete.application.port.out.EventRepository
import com.training.coach.athlete.application.port.out.FitnessPlatformPort
import com.training.coach.athlete.application.port.out.NotificationRepository
import com.training.coach.athlete.domain.model.Athlete
import com.training.coach.athlete.domain.model.Event
import com.training.coach.athlete.domain.model.Notification
import com.training.coach.athlete.domain.model.TrainingPlan
import com.training.coach.shared.functional.Result
import com.training.coach.trainingplan.application.port.out.PlanRepository
import com.training.coach.trainingplan.application.port.out.TrainingPlanRepository
import com.training.coach.trainingplan.domain.model.PlanSummary
import com.training.coach.trainingplan.domain.model.PlanVersion
import com.training.coach.trainingplan.domain.model.TrainingPlanSummary
import com.training.coach.trainingplan.infrastructure.persistence.entity.PlanVersionStatus
import com.training.coach.user.application.port.out.SystemUserRepository
import com.training.coach.user.domain.model.SystemUser
import com.training.coach.wellness.application.port.out.TrainingLoadRepository
import com.training.coach.wellness.application.port.out.WellnessRepository
import com.training.coach.wellness.domain.model.TrainingLoadSummary
import com.training.coach.wellness.domain.model.WellnessSnapshot
import io.cucumber.spring.CucumberContextConfiguration
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import io.cucumber.spring.ScenarioScope
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.test.context.ActiveProfiles

@CucumberContextConfiguration
@SpringBootTest(properties = ["intervals.icu.api-key=test"])
@ActiveProfiles("test")
@Import(CucumberTestConfig::class)
class CucumberSpringConfiguration

@TestConfiguration
@Profile("test")
open class CucumberTestConfig {
    @Bean
    open fun fitnessPlatformPort(): TestFitnessPlatformPort = TestFitnessPlatformPort()

    @Bean
    @ScenarioScope
    open fun systemUserRepository(): InMemorySystemUserRepository = InMemorySystemUserRepository()

    @Bean
    @ScenarioScope
    open fun userCredentialsRepository(): InMemoryUserCredentialsRepository = InMemoryUserCredentialsRepository()

    @Bean
    @ScenarioScope
    open fun activityRepository(): InMemoryActivityRepository = InMemoryActivityRepository()

    @Bean
    @ScenarioScope
    open fun athleteRepository(): InMemoryAthleteRepository = InMemoryAthleteRepository()

    @Bean
    @ScenarioScope
    open fun wellnessRepository(): InMemoryWellnessRepository = InMemoryWellnessRepository()

    @Bean
    @ScenarioScope
    open fun trainingLoadRepository(): InMemoryTrainingLoadRepository = InMemoryTrainingLoadRepository()

    @Bean
    @ScenarioScope
    open fun planRepository(): InMemoryPlanRepository = InMemoryPlanRepository()

    @Bean
    @ScenarioScope
    open fun trainingPlanRepository(): InMemoryTrainingPlanRepository = InMemoryTrainingPlanRepository()

    @Bean
    @ScenarioScope
    open fun refreshTokenStore(): InMemoryRefreshTokenStore = InMemoryRefreshTokenStore()

    @Bean
    @ScenarioScope
    open fun eventRepository(): InMemoryEventRepository = InMemoryEventRepository()

    @Bean
    @ScenarioScope
    open fun notificationRepository(): InMemoryNotificationRepository = InMemoryNotificationRepository()
}

open class InMemorySystemUserRepository : SystemUserRepository {
    private val users = ConcurrentHashMap<String, SystemUser>()

    override fun save(user: SystemUser): SystemUser {
        users[user.id()] = user
        return user
    }

    override fun findById(id: String) = java.util.Optional.ofNullable(users[id])

    override fun findAll(): List<SystemUser> = users.values.toList()

    override fun deleteById(id: String) {
        users.remove(id)
    }
}

open class InMemoryUserCredentialsRepository : com.training.coach.user.application.port.out.UserCredentialsRepository {
    private val credentialsById = ConcurrentHashMap<String, com.training.coach.user.application.port.out.UserCredentialsRepository.CredentialsRecord>()
    private val idByUsername = ConcurrentHashMap<String, String>()
    private val idByUserId = ConcurrentHashMap<String, String>()

    override fun save(
        userId: String,
        username: String,
        passwordHash: String,
        enabled: Boolean
    ): com.training.coach.user.application.port.out.UserCredentialsRepository.CredentialsRecord {
        val id = idByUserId[userId] ?: java.util.UUID.randomUUID().toString()
        val record = com.training.coach.user.application.port.out.UserCredentialsRepository.CredentialsRecord(
            id,
            userId,
            username,
            passwordHash,
            enabled
        )
        credentialsById[id] = record
        idByUsername[username] = id
        idByUserId[userId] = id
        return record
    }

    override fun findByUsername(username: String) = java.util.Optional.ofNullable(idByUsername[username])
        .map { credentialsById[it] }

    override fun findByUserId(userId: String) = java.util.Optional.ofNullable(idByUserId[userId])
        .map { credentialsById[it] }

    override fun updatePasswordHash(userId: String, passwordHash: String) {
        val id = idByUserId[userId] ?: return
        val record = credentialsById[id] ?: return
        credentialsById[id] = com.training.coach.user.application.port.out.UserCredentialsRepository.CredentialsRecord(
            record.id(),
            record.userId(),
            record.username(),
            passwordHash,
            record.enabled()
        )
    }

    override fun setEnabled(userId: String, enabled: Boolean) {
        val id = idByUserId[userId] ?: return
        val record = credentialsById[id] ?: return
        credentialsById[id] = com.training.coach.user.application.port.out.UserCredentialsRepository.CredentialsRecord(
            record.id(),
            record.userId(),
            record.username(),
            record.passwordHash(),
            enabled
        )
    }
}

open class InMemoryActivityRepository : ActivityRepository {
    private val byAthleteId = ConcurrentHashMap<String, MutableList<ActivityLight>>()

    override fun save(activity: ActivityLight): ActivityLight {
        byAthleteId.computeIfAbsent(activity.athleteId()) { mutableListOf() }
            .removeIf { it.externalActivityId() == activity.externalActivityId() }
        byAthleteId[activity.athleteId()]?.add(activity)
        return activity
    }

    override fun saveAll(activities: List<ActivityLight>): List<ActivityLight> {
        activities.forEach { save(it) }
        return activities
    }

    override fun findByAthleteIdAndExternalActivityId(athleteId: String, externalActivityId: String) =
        java.util.Optional.ofNullable(byAthleteId[athleteId]?.firstOrNull { it.externalActivityId() == externalActivityId })

    override fun findByAthleteIdAndDate(athleteId: String, date: LocalDate) =
        java.util.Optional.ofNullable(byAthleteId[athleteId]?.firstOrNull { it.date() == date })

    override fun findByAthleteIdAndDateRange(athleteId: String, startDate: LocalDate, endDate: LocalDate): List<ActivityLight> {
        val stored = byAthleteId[athleteId].orEmpty()
        return stored.filter { !it.date().isBefore(startDate) && !it.date().isAfter(endDate) }
    }
}

open class InMemoryAthleteRepository : AthleteRepository {
    private val athletes = ConcurrentHashMap<String, Athlete>()

    override fun save(athlete: Athlete): Athlete {
        athletes[athlete.id()] = athlete
        return athlete
    }

    override fun findById(id: String) = java.util.Optional.ofNullable(athletes[id])

    override fun deleteById(id: String) {
        athletes.remove(id)
    }

    override fun findAll(): List<Athlete> = athletes.values.toList()
}

open class InMemoryWellnessRepository : WellnessRepository {
    private val byAthlete = ConcurrentHashMap<String, MutableList<WellnessSnapshot>>()

    override fun findByAthleteIdAndDate(athleteId: String, date: LocalDate) =
        java.util.Optional.ofNullable(byAthlete[athleteId]?.firstOrNull { it.date() == date })

    override fun findByAthleteIdAndDateRange(
        athleteId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<WellnessSnapshot> {
        return byAthlete[athleteId].orEmpty()
            .filter { !it.date().isBefore(startDate) && !it.date().isAfter(endDate) }
    }

    override fun findLatestByAthleteId(athleteId: String) = java.util.Optional.ofNullable(
        byAthlete[athleteId].orEmpty().maxByOrNull { it.date() }
    )

    override fun save(snapshot: WellnessSnapshot): WellnessSnapshot {
        byAthlete.computeIfAbsent(snapshot.athleteId()) { mutableListOf() }
            .removeIf { it.date() == snapshot.date() }
        byAthlete[snapshot.athleteId()]?.add(snapshot)
        return snapshot
    }

    override fun saveAll(snapshots: List<WellnessSnapshot>): List<WellnessSnapshot> {
        snapshots.forEach { save(it) }
        return snapshots
    }

    override fun deleteByAthleteIdAndDate(athleteId: String, date: LocalDate) {
        byAthlete[athleteId]?.removeIf { it.date() == date }
    }

    override fun existsByAthleteIdAndDate(athleteId: String, date: LocalDate): Boolean {
        return byAthlete[athleteId].orEmpty().any { it.date() == date }
    }
}

open class InMemoryTrainingLoadRepository : TrainingLoadRepository {
    private val ctl = ConcurrentHashMap<String, MutableMap<LocalDate, Double>>()
    private val atl = ConcurrentHashMap<String, MutableMap<LocalDate, Double>>()
    private val summaries = ConcurrentHashMap<String, MutableMap<LocalDate, TrainingLoadSummary>>()

    override fun findTrainingLoadByAthleteIdAndDate(athleteId: String, date: LocalDate) =
        java.util.Optional.ofNullable(summaries[athleteId]?.get(date))

    override fun findCtlByAthleteIdAndDate(athleteId: String, date: LocalDate): Double {
        return ctl[athleteId]?.get(date) ?: 0.0
    }

    override fun findAtlByAthleteIdAndDate(athleteId: String, date: LocalDate): Double {
        return atl[athleteId]?.get(date) ?: 0.0
    }

    override fun saveCtl(athleteId: String, date: LocalDate, ctlValue: Double) {
        ctl.computeIfAbsent(athleteId) { mutableMapOf() }[date] = ctlValue
        summaries.computeIfAbsent(athleteId) { mutableMapOf() }
            .computeIfAbsent(date) { TrainingLoadSummary.empty() }
    }

    override fun saveAtl(athleteId: String, date: LocalDate, atlValue: Double) {
        atl.computeIfAbsent(athleteId) { mutableMapOf() }[date] = atlValue
        summaries.computeIfAbsent(athleteId) { mutableMapOf() }
            .computeIfAbsent(date) { TrainingLoadSummary.empty() }
    }
}

open class InMemoryPlanRepository : PlanRepository {
    private val plans = ConcurrentHashMap<String, PlanSummary>()
    private val versionsByPlan = ConcurrentHashMap<String, MutableList<PlanVersion>>()

    override fun save(plan: PlanSummary): PlanSummary {
        plans[plan.id()] = plan
        return plan
    }

    override fun findById(id: String) = java.util.Optional.ofNullable(plans[id])

    override fun findAll(): List<PlanSummary> = plans.values.toList()

    override fun saveVersion(version: PlanVersion): PlanVersion {
        versionsByPlan.computeIfAbsent(version.planId()) { mutableListOf() }.add(version)
        return version
    }

    override fun findVersion(planId: String, version: Int) = java.util.Optional.ofNullable(
        versionsByPlan[planId]?.firstOrNull { it.versionNumber() == version }
    )

    override fun findVersions(planId: String): List<PlanVersion> = versionsByPlan[planId].orEmpty().toList()

    override fun updateVersionStatus(planId: String, version: Int, status: PlanVersionStatus) {
        val versions = versionsByPlan[planId] ?: return
        val index = versions.indexOfFirst { it.versionNumber() == version }
        if (index >= 0) {
            val existing = versions[index]
            versions[index] = PlanVersion(existing.planId(), existing.versionNumber(), status, existing.workouts(), existing.createdAt())
        }
    }
}

open class InMemoryTrainingPlanRepository : TrainingPlanRepository {

    private val plans = ConcurrentHashMap<String, TrainingPlan>()

    override fun save(plan: TrainingPlan): TrainingPlan {
        plans[plan.id()] = plan
        return plan
    }

    override fun findById(id: String) = java.util.Optional.ofNullable(plans[id])

    override fun deleteById(id: String) {
        plans.remove(id)
    }

    override fun findByAthleteId(athleteId: String): List<TrainingPlanSummary> {
        return plans.values.filter { it.athleteId() == athleteId }.map {
            TrainingPlanSummary(it.id(), it.athleteId(), "Plan ${it.id().take(8)}", 1, PlanVersionStatus.PUBLISHED)
        }
    }
}

open class InMemoryRefreshTokenStore : com.training.coach.security.RefreshTokenStore {
    private val tokens = ConcurrentHashMap<String, com.training.coach.security.RefreshTokenStore.RefreshTokenRecord>()
    private val familyIndex =
        ConcurrentHashMap<String, MutableList<com.training.coach.security.RefreshTokenStore.RefreshTokenRecord>>()

    override fun save(
        record: com.training.coach.security.RefreshTokenStore.RefreshTokenRecord
    ): com.training.coach.security.RefreshTokenStore.RefreshTokenRecord {
        tokens[record.tokenHash()] = record
        familyIndex.computeIfAbsent(record.familyId()) { mutableListOf() }
            .removeIf { it.tokenHash() == record.tokenHash() }
        familyIndex[record.familyId()]?.add(record)
        return record
    }

    override fun findByTokenHash(tokenHash: String) = java.util.Optional.ofNullable(tokens[tokenHash])

    override fun findByFamilyId(
        familyId: String
    ): List<com.training.coach.security.RefreshTokenStore.RefreshTokenRecord> {
        return familyIndex[familyId].orEmpty().toList()
    }
}

open class TestFitnessPlatformPort : FitnessPlatformPort {
    private var activities: MutableList<FitnessPlatformPort.Activity>? = null
    private var wellnessData: MutableList<FitnessPlatformPort.WellnessData>? = null

    fun setActivities(newActivities: List<FitnessPlatformPort.Activity>) {
        ensureInitialized()
        activities?.clear()
        activities?.addAll(newActivities)
    }

    fun setWellnessData(newWellnessData: List<FitnessPlatformPort.WellnessData>) {
        ensureInitialized()
        wellnessData?.clear()
        wellnessData?.addAll(newWellnessData)
    }

    fun clear() {
        ensureInitialized()
        activities?.clear()
        wellnessData?.clear()
    }

    private fun ensureInitialized() {
        if (activities == null) {
            activities = mutableListOf()
        }
        if (wellnessData == null) {
            wellnessData = mutableListOf()
        }
    }

    override fun getActivities(
        athleteId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<List<FitnessPlatformPort.Activity>> {
        ensureInitialized()
        val filtered = activities.orEmpty().filter { !it.date().isBefore(startDate) && !it.date().isAfter(endDate) }
        return Result.success(filtered)
    }

    override fun getWellnessData(
        athleteId: String,
        date: LocalDate
    ): Result<FitnessPlatformPort.WellnessData> {
        ensureInitialized()
        val found = wellnessData.orEmpty().firstOrNull { it.date() == date }
            ?: return Result.failure(IllegalStateException("No wellness data for $date"))
        return Result.success(found)
    }

    override fun getWellnessDataRange(
        athleteId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<List<FitnessPlatformPort.WellnessData>> {
        ensureInitialized()
        val filtered = wellnessData.orEmpty().filter { !it.date().isBefore(startDate) && !it.date().isAfter(endDate) }
        return Result.success(filtered)
    }
}

open class InMemoryEventRepository : EventRepository {
    private val events = ConcurrentHashMap<String, MutableList<Event>>()

    override fun save(event: Event): Event {
        events.computeIfAbsent(event.athleteId()) { mutableListOf() }.add(event)
        return event
    }

    override fun findByAthleteId(athleteId: String): List<Event> = events[athleteId].orEmpty().toList()
}

open class InMemoryNotificationRepository : NotificationRepository {
    private val notifications = ConcurrentHashMap<String, MutableList<Notification>>()

    override fun save(notification: Notification): Notification {
        notifications.computeIfAbsent(notification.athleteId()) { mutableListOf() }.add(notification)
        return notification
    }

    override fun findByAthleteId(athleteId: String): List<Notification> = notifications[athleteId].orEmpty().toList()
}
