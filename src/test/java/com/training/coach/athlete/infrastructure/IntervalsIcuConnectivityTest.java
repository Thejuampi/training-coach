package com.training.coach.athlete.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = "spring.profiles.active=test")
@EnabledIfEnvironmentVariable(named = "INTERVALS_ICU_API_KEY", matches = ".+")
@DisplayName("Intervals.icu connectivity")
class IntervalsIcuConnectivityTest {

    @Value("${intervals.icu.api-key}")
    private String apiKey;

    @Value("${intervals.icu.base-url:https://intervals.icu}")
    private String baseUrl;

    @Value("${intervals.icu.athlete-id}")
    private String athleteId;

    @Test
    @DisplayName("Should connect with injected API key")
    void shouldConnectWithInjectedApiKey() {
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> {
                    headers.setBasicAuth("API_KEY", apiKey);
                    headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                })
                .build();

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(1);
        Boolean ok = webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/athlete/{athleteId}/activities")
                        .queryParam("oldest", startDate)
                        .queryParam("newest", endDate)
                        .build(athleteId))
                .exchangeToMono(response -> Mono.just(response.statusCode().is2xxSuccessful()))
                .block(Duration.ofSeconds(10));

        assertThat(ok).isTrue();
    }
}
