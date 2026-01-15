package com.training.coach.analysis.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Adjustment Service Tests")
class AdjustmentServiceTest {

    @Test
    @DisplayName("Should suggest reducing volume for low readiness")
    void shouldSuggestReducingVolumeForLowReadiness() {
        // Given
        AdjustmentService service = new AdjustmentService();
        double readinessScore = 3.0; // Low
        double compliance = 90.0;

        // When
        String suggestion = service.suggestAdjustment(readinessScore, compliance);

        // Then
        assertThat(suggestion.toLowerCase()).contains("reduce volume");
    }

    @Test
    @DisplayName("Should suggest no change for good readiness")
    void shouldSuggestNoChangeForGoodReadiness() {
        // Given
        AdjustmentService service = new AdjustmentService();
        double readinessScore = 8.0; // Good
        double compliance = 95.0;

        // When
        String suggestion = service.suggestAdjustment(readinessScore, compliance);

        // Then
        assertThat(suggestion.toLowerCase()).contains("maintain");
    }
}
