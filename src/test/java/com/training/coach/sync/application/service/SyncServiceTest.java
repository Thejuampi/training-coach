package com.training.coach.sync.application.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.training.coach.activity.application.port.out.ActivityRepository;
import com.training.coach.athlete.application.port.out.FitnessPlatformPort;
import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.HeartRateVariability;
import com.training.coach.shared.domain.unit.Hours;
import com.training.coach.shared.domain.unit.Kilograms;
import com.training.coach.shared.functional.Result;
import com.training.coach.wellness.application.port.out.WellnessRepository;
import com.training.coach.wellness.application.service.ReadinessCalculatorService;
import com.training.coach.wellness.application.service.TrainingLoadCalculator;
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

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private WellnessRepository wellnessRepository;

    @Mock
    private ReadinessCalculatorService readinessCalculatorService;

    @Mock
    private TrainingLoadCalculator trainingLoadCalculator;

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
        when(activityRepository.saveAll(List.of())).thenReturn(List.of());
        when(fitnessPlatformPort.getWellnessDataRange(athleteId, startDate, endDate))
                .thenReturn(Result.success(List.of(new FitnessPlatformPort.WellnessData(
                        endDate,
                        BeatsPerMinute.of(60.0),
                        HeartRateVariability.of(50.0),
                        Kilograms.of(75.0),
                        Hours.of(8.0),
                        7))));
        when(trainingLoadCalculator.calculateTrainingLoadSummary(athleteId, endDate))
                .thenReturn(com.training.coach.wellness.domain.model.TrainingLoadSummary.empty());
        when(readinessCalculatorService.calculateReadiness(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.isNull(),
                        org.mockito.ArgumentMatchers.any()))
                .thenReturn(50.0);

        // When
        syncService.syncAthleteData(athleteId, startDate, endDate);

        // Then
        verify(fitnessPlatformPort).getActivities(athleteId, startDate, endDate);
        verify(fitnessPlatformPort).getWellnessDataRange(athleteId, startDate, endDate);
    }
}
