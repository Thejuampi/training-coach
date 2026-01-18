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

class TestFitnessPlatformPort : FitnessPlatformPort {
    private val activities: MutableList<FitnessPlatformPort.Activity> = mutableListOf()
    private val wellnessData: MutableList<FitnessPlatformPort.WellnessData> = mutableListOf()

    fun setActivities(newActivities: List<FitnessPlatformPort.Activity>) {
        activities.clear()
        activities.addAll(newActivities)
    }

    fun setWellnessData(newWellnessData: List<FitnessPlatformPort.WellnessData>) {
        wellnessData.clear()
        wellnessData.addAll(newWellnessData)
    }

    fun clear() {
        activities.clear()
        wellnessData.clear()
    }

    override fun getActivities(
        athleteId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<List<FitnessPlatformPort.Activity>> {
        val filtered = activities.filter { !it.date().isBefore(startDate) && !it.date().isAfter(endDate) }
        return Result.success(filtered)
    }

    override fun getWellnessData(
        athleteId: String,
        date: LocalDate
    ): Result<FitnessPlatformPort.WellnessData> {
        val found = wellnessData.firstOrNull { it.date() == date }
            ?: return Result.failure(IllegalStateException("No wellness data for $date"))
        return Result.success(found)
    }

    override fun getWellnessDataRange(
        athleteId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<List<FitnessPlatformPort.WellnessData>> {
        val filtered = wellnessData.filter { !it.date().isBefore(startDate) && !it.date().isAfter(endDate) }
        return Result.success(filtered)
    }
}
