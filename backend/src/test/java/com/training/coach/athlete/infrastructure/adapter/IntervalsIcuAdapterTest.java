package com.training.coach.athlete.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.training.coach.shared.functional.Result;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("IntervalsIcuAdapter retry behavior")
class IntervalsIcuAdapterTest {

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("Retries after transient server failures")
    void retriesOnServerErrors() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("temporary"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("[]"));

        IntervalsIcuAdapter adapter = new IntervalsIcuAdapter(
                "api-key", mockWebServer.url("/").toString(), 2, Duration.ofMillis(10), Duration.ofMillis(50));

        Result<?> result = adapter.getActivities("athlete", LocalDate.now(), LocalDate.now());

        assertThat(result.isFailure()).isTrue();
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not retry on client errors")
    void doesNotRetryOnClientErrors() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(400).setBody("bad request"));

        IntervalsIcuAdapter adapter = new IntervalsIcuAdapter(
                "api-key", mockWebServer.url("/").toString(), 2, Duration.ofMillis(10), Duration.ofMillis(50));

        Result<?> result = adapter.getActivities("athlete", LocalDate.now(), LocalDate.now());

        assertThat(result.isFailure()).isTrue();
        assertThat(mockWebServer.getRequestCount()).isEqualTo(1);
    }
}
