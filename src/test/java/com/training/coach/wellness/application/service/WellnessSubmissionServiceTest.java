package com.training.coach.wellness.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.HeartRateVariability;
import com.training.coach.shared.domain.unit.Kilograms;
import com.training.coach.wellness.application.port.out.TrainingLoadRepository;
import com.training.coach.wellness.application.port.out.WellnessRepository;
import com.training.coach.wellness.domain.model.PhysiologicalData;
import com.training.coach.wellness.domain.model.SubjectiveWellness;
import com.training.coach.wellness.domain.model.WellnessSnapshot;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("WellnessSubmissionService Tests")
class WellnessSubmissionServiceTest {

    @Mock
    private WellnessRepository wellnessRepository;

    @Mock
    private TrainingLoadRepository trainingLoadRepository;

    private ReadinessCalculatorService readinessCalculator;
    private WellnessSubmissionService service;

    @BeforeEach
    void setUp() {
        readinessCalculator = new ReadinessCalculatorService();
        service = new WellnessSubmissionService(wellnessRepository, readinessCalculator, trainingLoadRepository);
    }

    @Test
    @DisplayName("Should submit wellness and calculate readiness score")
    void shouldSubmitWellnessAndCalculateReadiness() {
        var subjective = SubjectiveWellness.create(5, 5, 7, 8, 4);
        var physiological =
                new PhysiologicalData(BeatsPerMinute.of(55), HeartRateVariability.of(60), Kilograms.of(75), null);

        var expectedSnapshot =
                WellnessSnapshot.create("athlete-1", LocalDate.now(), physiological, subjective, null, 0.0);

        org.mockito.Mockito.when(
                        trainingLoadRepository.findTrainingLoadByAthleteIdAndDate("athlete-1", LocalDate.now()))
                .thenReturn(Optional.empty());
        org.mockito.Mockito.when(wellnessRepository.save(org.mockito.Mockito.any()))
                .thenReturn(expectedSnapshot);

        var result = service.submitWellness("athlete-1", LocalDate.now(), subjective, physiological);

        assertThat(result).isNotNull();
        assertThat(result.athleteId()).isEqualTo("athlete-1");
        org.mockito.Mockito.verify(wellnessRepository).save(org.mockito.Mockito.any());
    }

    @Test
    @DisplayName("Should get wellness history for athlete")
    void shouldGetWellnessHistory() {
        var snapshots = java.util.List.of(
                WellnessSnapshot.create("athlete-1", LocalDate.now().minusDays(1), null, null, null, 75.0),
                WellnessSnapshot.create("athlete-1", LocalDate.now(), null, null, null, 80.0));

        org.mockito.Mockito.when(wellnessRepository.findByAthleteIdAndDateRange(
                        "athlete-1", LocalDate.now().minusDays(7), LocalDate.now()))
                .thenReturn(snapshots);

        var result = service.getWellnessHistory("athlete-1", LocalDate.now().minusDays(7), LocalDate.now());

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should return latest wellness for athlete")
    void shouldReturnLatestWellness() {
        var latestSnapshot = WellnessSnapshot.create("athlete-1", LocalDate.now(), null, null, null, 85.0);

        org.mockito.Mockito.when(wellnessRepository.findLatestByAthleteId("athlete-1"))
                .thenReturn(Optional.of(latestSnapshot));

        var result = service.getLatestWellness("athlete-1");

        assertThat(result).isPresent();
        assertThat(result.get().readinessScore()).isEqualTo(85.0);
    }

    @Test
    @DisplayName("Should return empty when no wellness data exists")
    void shouldReturnEmptyWhenNoData() {
        org.mockito.Mockito.when(wellnessRepository.findLatestByAthleteId("athlete-1"))
                .thenReturn(Optional.empty());

        var result = service.getLatestWellness("athlete-1");

        assertThat(result).isEmpty();
    }
}
