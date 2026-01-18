package com.training.coach.acceptance

import com.training.coach.athlete.application.port.out.FitnessPlatformPort
import com.training.coach.shared.functional.Result
import io.cucumber.spring.CucumberContextConfiguration
import java.time.LocalDate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import io.cucumber.spring.ScenarioScope
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@CucumberContextConfiguration
@SpringBootTest(properties = ["intervals.icu.api-key=test"])
@ActiveProfiles("test")
@Import(CucumberTestConfig::class)
class CucumberSpringConfiguration

@TestConfiguration
open class CucumberTestConfig {
    @Bean
    @Primary
    @ScenarioScope
    open fun fitnessPlatformPort(): TestFitnessPlatformPort = TestFitnessPlatformPort()
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
