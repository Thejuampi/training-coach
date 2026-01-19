package com.training.coach.acceptance

import com.training.coach.athlete.application.port.out.FitnessPlatformPort
import com.training.coach.testconfig.inmemory.InMemoryActivityRepository
import com.training.coach.testconfig.inmemory.InMemoryAthleteRepository
import com.training.coach.testconfig.inmemory.InMemoryEventRepository
import com.training.coach.testconfig.inmemory.InMemoryNotificationRepository
import com.training.coach.testconfig.inmemory.InMemoryPlanRepository
import com.training.coach.testconfig.inmemory.InMemoryRefreshTokenStore
import com.training.coach.testconfig.inmemory.InMemorySystemUserRepository
import com.training.coach.testconfig.inmemory.InMemoryTrainingLoadRepository
import com.training.coach.testconfig.inmemory.InMemoryTrainingPlanRepository
import com.training.coach.testconfig.inmemory.InMemoryUserCredentialsRepository
import com.training.coach.testconfig.inmemory.InMemoryWellnessRepository
import com.training.coach.testconfig.inmemory.TestFitnessPlatformPort
import io.cucumber.spring.CucumberContextConfiguration
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
