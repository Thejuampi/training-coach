package com.training.coach.testconfig;

import com.training.coach.activity.application.port.out.ActivityRepository;
import com.training.coach.athlete.application.port.out.AthleteRepository;
import com.training.coach.athlete.application.port.out.EventRepository;
import com.training.coach.athlete.application.port.out.FitnessPlatformPort;
import com.training.coach.athlete.application.port.out.NotificationRepository;
import com.training.coach.privacy.application.port.out.ConsentLogRepository;
import com.training.coach.privacy.application.port.out.DataDeletionRequestRepository;
import com.training.coach.privacy.application.port.out.DataExportRequestRepository;
import com.training.coach.reporting.application.port.out.WeeklyReportRepository;
import com.training.coach.security.RefreshTokenStore;
import com.training.coach.testconfig.inmemory.InMemoryActivityRepository;
import com.training.coach.testconfig.inmemory.InMemoryAthleteRepository;
import com.training.coach.testconfig.inmemory.InMemoryEventRepository;
import com.training.coach.reporting.application.port.out.WeeklyReportRepository;
import com.training.coach.testconfig.inmemory.InMemoryConsentLogRepository;
import com.training.coach.testconfig.inmemory.InMemoryDataDeletionRequestRepository;
import com.training.coach.testconfig.inmemory.InMemoryDataExportRequestRepository;
import com.training.coach.testconfig.inmemory.InMemoryNotificationRepository;
import com.training.coach.testconfig.inmemory.InMemoryPlanRepository;
import com.training.coach.testconfig.inmemory.InMemoryWeeklyReportRepository;
import com.training.coach.testconfig.inmemory.InMemoryRefreshTokenStore;
import com.training.coach.testconfig.inmemory.InMemorySystemUserRepository;
import com.training.coach.testconfig.inmemory.InMemoryTrainingLoadRepository;
import com.training.coach.testconfig.inmemory.InMemoryTrainingPlanRepository;
import com.training.coach.testconfig.inmemory.InMemoryUserCredentialsRepository;
import com.training.coach.testconfig.inmemory.InMemoryWellnessRepository;
import com.training.coach.testconfig.inmemory.TestFitnessPlatformPort;
import com.training.coach.trainingplan.application.port.out.PlanRepository;
import com.training.coach.trainingplan.application.port.out.TrainingPlanRepository;
import com.training.coach.user.application.port.out.SystemUserRepository;
import com.training.coach.user.application.port.out.UserCredentialsRepository;
import com.training.coach.wellness.application.port.out.TrainingLoadRepository;
import com.training.coach.wellness.application.port.out.WellnessRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/**
 * Test configuration providing in-memory repository implementations.
 *
 * <p>Use this config for fast tests that should avoid external IO while still wiring
 * the core application services. This is intended for tests that need repository-backed
 * behavior without mocks.</p>
 *
 * <p>All beans here are guarded by the {@code test} profile to avoid instantiating production
 * adapters. The production equivalents should use {@code @Profile("!test")} so Spring does
 * not create and then override them during tests.</p>
 */
@TestConfiguration
@Profile("test")
public class InMemoryRepositoriesTestConfig {

    @Bean
    public SystemUserRepository systemUserRepository() {
        return new InMemorySystemUserRepository();
    }

    @Bean
    public UserCredentialsRepository userCredentialsRepository() {
        return new InMemoryUserCredentialsRepository();
    }

    @Bean
    public ActivityRepository activityRepository() {
        return new InMemoryActivityRepository();
    }

    @Bean
    public AthleteRepository athleteRepository() {
        return new InMemoryAthleteRepository();
    }

    @Bean
    public WellnessRepository wellnessRepository() {
        return new InMemoryWellnessRepository();
    }

    @Bean
    public TrainingLoadRepository trainingLoadRepository() {
        return new InMemoryTrainingLoadRepository();
    }

    @Bean
    public PlanRepository planRepository() {
        return new InMemoryPlanRepository();
    }

    @Bean
    public TrainingPlanRepository trainingPlanRepository() {
        return new InMemoryTrainingPlanRepository();
    }

    @Bean
    public RefreshTokenStore refreshTokenStore() {
        return new InMemoryRefreshTokenStore();
    }

    @Bean
    public FitnessPlatformPort fitnessPlatformPort() {
        return new TestFitnessPlatformPort();
    }

    @Bean
    public EventRepository eventRepository() {
        return new InMemoryEventRepository();
    }

    @Bean
    public NotificationRepository notificationRepository() {
        return new InMemoryNotificationRepository();
    }

    @Bean
    public WeeklyReportRepository weeklyReportRepository() {
        return new InMemoryWeeklyReportRepository();
    }

    @Bean
    public DataExportRequestRepository dataExportRequestRepository() {
        return new InMemoryDataExportRequestRepository();
    }

    @Bean
    public DataDeletionRequestRepository dataDeletionRequestRepository() {
        return new InMemoryDataDeletionRequestRepository();
    }

    @Bean
    public ConsentLogRepository consentLogRepository() {
        return new InMemoryConsentLogRepository();
    }
}
