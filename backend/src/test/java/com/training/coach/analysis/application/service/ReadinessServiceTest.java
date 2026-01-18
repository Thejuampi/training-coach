package com.training.coach.analysis.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.training.coach.athlete.domain.model.ReadinessSnapshot;
import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.HeartRateVariability;
import com.training.coach.shared.domain.unit.Hours;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Readiness Service Tests")
class ReadinessServiceTest {

    @Test
    @DisplayName("Should calculate readiness score from wellness data")
    void shouldCalculateReadinessScore() {
        // Given
        ReadinessService service = new ReadinessService();
        // Mock or dummy wellness data
        BeatsPerMinute rhr = BeatsPerMinute.of(60.0);
        HeartRateVariability hrv = HeartRateVariability.of(50.0);
        Hours sleepHours = Hours.of(8.0);
        int sleepQuality = 8;

        // When
        ReadinessSnapshot snapshot = service.calculateReadiness(rhr, hrv, sleepHours, sleepQuality);

        // Then
        assertThat(snapshot).isNotNull();
        assertThat(snapshot.readinessScore()).isBetween(0.0, 10.0);
    }
}
