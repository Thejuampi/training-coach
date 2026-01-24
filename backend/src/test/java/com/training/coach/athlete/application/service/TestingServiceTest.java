package com.training.coach.athlete.application.service;

import com.training.coach.athlete.application.port.out.AthleteRepository;
import com.training.coach.athlete.application.port.out.TestResultRepository;
import com.training.coach.athlete.domain.model.Athlete;
import com.training.coach.athlete.domain.model.AthleteProfile;
import com.training.coach.athlete.domain.model.FtpTestResult;
import com.training.coach.athlete.domain.model.TrainingMetrics;
import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.Watts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestingServiceTest {

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private FtpTestService ftpTestService;

    @Mock
    private TestResultRepository testResultRepository;

    private TestingService testingService;

    @BeforeEach
    void setUp() {
        testingService = new TestingService(
                athleteRepository,
                ftpTestService,
                testResultRepository
        );
    }

    @Test
    void scheduleFtpTest_shouldScheduleTestForAthlete() {
        // Given
        String athleteId = "athlete-123";
        Athlete athlete = createTestAthlete(athleteId);
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(athlete));

        // When
        LocalDate testDate = LocalDate.now().plusDays(7);
        testingService.scheduleFtpTest(athleteId, testDate);

        // Then
        assertThat(testingService.getScheduledTests(athleteId)).hasSize(1);
        assertThat(testingService.getScheduledTests(athleteId).get(0).date()).isEqualTo(testDate);
        assertThat(testingService.getScheduledTests(athleteId).get(0).type()).isEqualTo(TestingService.TestType.FTP_TEST);
        assertThat(testingService.getScheduledTests(athleteId).get(0).status()).isEqualTo(TestingService.TestStatus.SCHEDULED);
    }

    @Test
    void recordFtpTestResult_shouldUpdateAthleteAndStoreResult() {
        // Given
        String athleteId = "athlete-123";
        Athlete athlete = createTestAthlete(athleteId);
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(athlete));
        when(athleteRepository.save(any())).thenReturn(athlete);

        LocalDate testDate = LocalDate.now();
        double ftpValue = 250.0;

        // When
        FtpTestResult result = testingService.recordFtpTestResult(athleteId, testDate, ftpValue);

        // Then
        assertThat(result.ftp().value()).isEqualTo(ftpValue);
        assertThat(result.testDate()).isEqualTo(testDate);
        assertThat(result.method()).isEqualTo(FtpTestResult.TestMethod.FIELD_20MIN);
        assertThat(result.confidencePercent()).isEqualTo(85.0);

        verify(athleteRepository).save(any(Athlete.class));
        verify(testResultRepository).save(result);
    }

    @Test
    void getHistoricalTestResults_shouldReturnAllResultsForAthlete() {
        // Given
        String athleteId = "athlete-123";
        FtpTestResult result1 = new FtpTestResult(
                Watts.of(220.0),
                LocalDate.now().minusDays(10),
                FtpTestResult.TestMethod.FIELD_20MIN,
                85.0
        );
        FtpTestResult result2 = new FtpTestResult(
                Watts.of(250.0),
                LocalDate.now(),
                FtpTestResult.TestMethod.LAB_LACTATE,
                95.0
        );

        when(testResultRepository.findByAthleteId(athleteId)).thenReturn(List.of(result1, result2));

        // When
        List<FtpTestResult> results = testingService.getHistoricalTestResults(athleteId);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).containsExactly(result1, result2);
    }

    @Test
    void getLatestFtpTestResult_shouldReturnMostRecentResult() {
        // Given
        String athleteId = "athlete-123";
        FtpTestResult oldResult = new FtpTestResult(
                Watts.of(220.0),
                LocalDate.now().minusDays(10),
                FtpTestResult.TestMethod.FIELD_20MIN,
                85.0
        );
        FtpTestResult newResult = new FtpTestResult(
                Watts.of(250.0),
                LocalDate.now(),
                FtpTestResult.TestMethod.LAB_LACTATE,
                95.0
        );

        when(testResultRepository.findByAthleteId(athleteId)).thenReturn(List.of(oldResult, newResult));

        // When
        Optional<FtpTestResult> latest = testingService.getLatestFtpTestResult(athleteId);

        // Then
        assertThat(latest).isPresent();
        assertThat(latest.get()).isEqualTo(newResult);
        assertThat(latest.get().ftp().value()).isEqualTo(250.0);
    }

    @Test
    void getTestInstructions_shouldReturnFtpTestInstructions() {
        // When
        String instructions = testingService.getTestInstructions(TestingService.TestType.FTP_TEST);

        // Then
        assertThat(instructions).contains("FTP Test Instructions:");
        assertThat(instructions).contains("Warm up for 15-20 minutes");
        assertThat(instructions).contains("20-minute time trial");
        assertThat(instructions).contains("95% of your 20-minute average power");
    }

    @Test
    void needsZoneRecalculation_shouldReturnTrueWhenFutureWorkoutsExist() {
        // Given
        String athleteId = "athlete-123";
        LocalDate ftpUpdateDate = LocalDate.now();

        // When
        boolean needsRecalculation = testingService.needsZoneRecalculation(athleteId, ftpUpdateDate);

        // Then
        assertThat(needsRecalculation).isTrue();
    }

    private Athlete createTestAthlete(String athleteId) {
        AthleteProfile profile = new AthleteProfile(
                "unspec",
                30,
                com.training.coach.shared.domain.unit.Kilograms.of(75.0),
                com.training.coach.shared.domain.unit.Centimeters.of(175.0),
                "intermediate"
        );

        TrainingMetrics metrics = new TrainingMetrics(
                Watts.of(200.0),
                BeatsPerMinute.of(170.0),
                com.training.coach.shared.domain.unit.VO2Max.of(50.0),
                com.training.coach.shared.domain.unit.Kilograms.of(75.0)
        );

        return new Athlete(
                athleteId,
                "Test Athlete",
                profile,
                metrics,
                null
        );
    }
}