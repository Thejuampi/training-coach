package com.training.coach.sync.application.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.training.coach.athlete.application.port.out.FitnessPlatformPort;
import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.HeartRateVariability;
import com.training.coach.shared.domain.unit.Hours;
import com.training.coach.shared.domain.unit.Kilograms;
import com.training.coach.shared.functional.Result;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Sync Service Tests")
class SyncServiceTest {

    @Mock
    private FitnessPlatformPort fitnessPlatformPort;

    @InjectMocks
    private SyncService syncService;

    @Test
    @DisplayName("Should sync athlete data from fitness platform")
    void shouldSyncAthleteData() {
        // Given
        String athleteId = "test-athlete";
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        when(fitnessPlatformPort.getActivities(athleteId, startDate, endDate)).thenReturn(Result.success(List.of()));
        when(fitnessPlatformPort.getWellnessData(athleteId, endDate))
                .thenReturn(Result.success(new FitnessPlatformPort.WellnessData(
                        endDate,
                        BeatsPerMinute.of(60.0),
                        HeartRateVariability.of(50.0),
                        Kilograms.of(75.0),
                        Hours.of(8.0),
                        7)));

        // When
        syncService.syncAthleteData(athleteId, startDate, endDate);

        // Then
        verify(fitnessPlatformPort).getActivities(athleteId, startDate, endDate);
        verify(fitnessPlatformPort).getWellnessData(athleteId, endDate);
    }
}
