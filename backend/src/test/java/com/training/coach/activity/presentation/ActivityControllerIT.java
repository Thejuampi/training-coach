package com.training.coach.activity.presentation;

import com.training.coach.activity.application.service.ActivityReadService;
import com.training.coach.activity.domain.model.ActivityLight;
import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.Kilometers;
import com.training.coach.shared.domain.unit.Seconds;
import com.training.coach.shared.domain.unit.Watts;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import com.training.coach.AbstractWebFluxControllerTest;
import com.training.coach.testconfig.ExternalPortsTestConfig;
import com.training.coach.testconfig.WebTestConfig;
import com.training.coach.testconfig.inmemory.InMemoryActivityRepository;

@SpringBootTest(properties = "intervals.icu.api-key=test", webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import({WebTestConfig.class, ExternalPortsTestConfig.class, ActivityControllerIT.TestServicesConfig.class})

class ActivityControllerIT extends AbstractWebFluxControllerTest {

    @org.springframework.beans.factory.annotation.Autowired
    private InMemoryActivityRepository activityRepository;

    @TestConfiguration
    static class TestServicesConfig {
        @Bean
        ActivityReadService activityReadService(InMemoryActivityRepository activityRepository) {
            return new ActivityReadService(activityRepository);
        }
    }

    @Test
    void getActivitiesReturnsHistory() {
        String athleteId = "ath-1";
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 7);
        ActivityLight activity = ActivityLight.create(
                athleteId,
                "act-1",
                LocalDate.of(2026, 1, 3),
                "Tempo Ride",
                "Ride",
                Seconds.of(3600),
                Kilometers.of(35.0),
                Watts.of(210.0),
                BeatsPerMinute.of(150.0),
                75.0,
                0.85,
                Watts.of(230.0));

        activityRepository.save(activity);

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/activities/athletes/{athleteId}")
                        .queryParam("startDate", startDate)
                        .queryParam("endDate", endDate)
                        .build(athleteId))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(ActivityLight.class)
                .hasSize(1);
    }

    @Test
    void getActivitiesRejectsLargeRange() {
        String athleteId = "ath-1";
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/activities/athletes/{athleteId}")
                        .queryParam("startDate", startDate)
                        .queryParam("endDate", endDate)
                        .build(athleteId))
                .exchange()
                .expectStatus()
                .isBadRequest();
    }


    @Test
    void getActivityByDateReturnsActivity() {
        String athleteId = "ath-1";
        LocalDate date = LocalDate.of(2026, 1, 3);
        ActivityLight activity = ActivityLight.create(
                athleteId,
                "act-1",
                date,
                "Tempo Ride",
                "Ride",
                Seconds.of(3600),
                Kilometers.of(35.0),
                Watts.of(210.0),
                BeatsPerMinute.of(150.0),
                75.0,
                0.85,
                Watts.of(230.0));

        activityRepository.save(activity);

        webTestClient
                .get()
                .uri("/api/activities/athletes/{athleteId}/date/{date}", athleteId, date)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ActivityLight.class);
    }

    @Test
    void getActivityByDateReturnsNotFound() {
        String athleteId = "ath-1";
        LocalDate date = LocalDate.of(2026, 1, 3);

        webTestClient
                .get()
                .uri("/api/activities/athletes/{athleteId}/date/{date}", athleteId, date)
                .exchange()
                .expectStatus()
                .isNotFound();
    }
}
