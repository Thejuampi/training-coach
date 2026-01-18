package com.training.coach.analysis.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Compliance Service Tests")
class ComplianceServiceTest {

    @Test
    @DisplayName("Should calculate compliance percentage")
    void shouldCalculateCompliance() {
        // Given
        ComplianceService service = new ComplianceService();
        int plannedWorkouts = 10;
        int completedWorkouts = 8;

        // When
        double compliance = service.calculateCompliance(plannedWorkouts, completedWorkouts);

        // Then
        assertThat(compliance).isEqualTo(80.0);
    }
}
