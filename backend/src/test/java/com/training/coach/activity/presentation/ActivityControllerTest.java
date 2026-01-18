package com.training.coach.activity.presentation;

import static org.mockito.Mockito.when;

import com.training.coach.activity.application.service.ActivityReadService;
import com.training.coach.activity.domain.model.ActivityLight;
import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.Kilometers;
import com.training.coach.shared.domain.unit.Seconds;
import com.training.coach.shared.domain.unit.Watts;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(properties = "intervals.icu.api-key=test")
@Import(ActivityControllerTest.TestSecurityConfig.class)
class ActivityControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ActivityReadService activityReadService;

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

        when(activityReadService.getActivities(athleteId, startDate, endDate)).thenReturn(List.of(activity));

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

        when(activityReadService.getActivities(athleteId, startDate, endDate))
                .thenThrow(new IllegalArgumentException("Date range cannot exceed 365 days"));

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

        when(activityReadService.getActivityByDate(athleteId, date)).thenReturn(Optional.of(activity));

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

        when(activityReadService.getActivityByDate(athleteId, date)).thenReturn(Optional.empty());

        webTestClient
                .get()
                .uri("/api/activities/athletes/{athleteId}/date/{date}", athleteId, date)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        @Order(0)
        SecurityWebFilterChain testSecurityWebFilterChain(ServerHttpSecurity http) {
            return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                    .authorizeExchange(exchange -> exchange.anyExchange().permitAll())
                    .build();
        }
    }
}
