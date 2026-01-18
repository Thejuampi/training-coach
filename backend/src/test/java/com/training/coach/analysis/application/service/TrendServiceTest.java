package com.training.coach.analysis.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Trend Service Tests")
class TrendServiceTest {

    @Test
    @DisplayName("Should calculate upward trend")
    void shouldCalculateUpwardTrend() {
        // Given
        TrendService service = new TrendService();
        List<Double> values = List.of(1.0, 2.0, 3.0, 4.0);

        // When
        String trend = service.calculateTrend(values);

        // Then
        assertThat(trend).isEqualTo("upward");
    }

    @Test
    @DisplayName("Should calculate downward trend")
    void shouldCalculateDownwardTrend() {
        // Given
        TrendService service = new TrendService();
        List<Double> values = List.of(4.0, 3.0, 2.0, 1.0);

        // When
        String trend = service.calculateTrend(values);

        // Then
        assertThat(trend).isEqualTo("downward");
    }
}
