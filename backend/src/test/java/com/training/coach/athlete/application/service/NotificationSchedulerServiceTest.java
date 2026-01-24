package com.training.coach.athlete.application.service;

import com.training.coach.athlete.application.port.out.AthleteRepository;
import com.training.coach.athlete.application.port.out.PlanRepository;
import com.training.coach.athlete.application.port.out.WorkoutExecutionRepository;
import com.training.coach.athlete.application.port.out.WellnessRepository;
import com.training.coach.athlete.domain.model.Athlete;
import com.training.coach.athlete.domain.model.AthleteId;
import com.training.coach.athlete.domain.model.AthleteReadiness;
import com.training.coach.plan.application.port.out.PlanRepository;
import com.training.coach.plan.domain.model.Plan;
import com.training.coach.plan.domain.model.Workout;
import com.training.coach.wellness.application.port.out.WellnessRepository;
import com.training.coach.wellness.domain.model.WellnessSnapshot;
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
class NotificationSchedulerServiceTest {

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private WorkoutExecutionRepository workoutExecutionRepository;

    @Mock
    private WellnessRepository wellnessRepository;

    @Mock
    private NotificationService notificationService;

    private NotificationSchedulerService notificationSchedulerService;

    @BeforeEach
    void setUp() {
        notificationSchedulerService = new NotificationSchedulerService(
                athleteRepository,
                planRepository,
                workoutExecutionRepository,
                wellnessRepository,
                notificationService
        );
    }

    @Test
    void sendDailyWorkoutReminders_shouldSendReminderWhenWorkoutExists() {
        // Given
        String athleteId = "athlete-123";
        Athlete athlete = new Athlete(athleteId, "Test Athlete", null, null, null, null);

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Workout workout = new Workout("workout-1", tomorrow, "ENDURANCE", 60, "moderate");
        Plan plan = new Plan("plan-1", athleteId, List.of(workout), null, null, null, null, null, null, null);

        when(athleteRepository.findAll()).thenReturn(List.of(athlete));
        when(planRepository.findByAthleteId(new AthleteId(athleteId))).thenReturn(List.of(plan));

        // When
        notificationSchedulerService.sendDailyWorkoutReminders();

        // Then
        verify(notificationService).notifyAthlete(athleteId, any(String.class));
    }

    @Test
    void sendDailyWorkoutReminders_shouldNotSendReminderWhenNoWorkoutExists() {
        // Given
        String athleteId = "athlete-123";
        Athlete athlete = new Athlete(athleteId, "Test Athlete", null, null, null, null);

        when(athleteRepository.findAll()).thenReturn(List.of(athlete));
        when(planRepository.findByAthleteId(new AthleteId(athleteId))).thenReturn(List.of());

        // When
        notificationSchedulerService.sendDailyWorkoutReminders();

        // Then
        verify(notificationService, never()).notifyAthlete(any(), any());
    }

    @Test
    void sendMissedSessionAlerts_shouldSendAlertWhenMissedKeySession() {
        // Given
        String athleteId = "athlete-123";
        Athlete athlete = new Athlete(athleteId, "Test Athlete", null, null, null, null);

        LocalDate startOfWeek = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        when(athleteRepository.findAll()).thenReturn(List.of(athlete));
        when(planRepository.findByAthleteId(new AthleteId(athleteId))).thenReturn(List.of(createMockPlan()));
        when(workoutExecutionRepository.findByAthleteIdAndDateRange(
                new AthleteId(athleteId),
                startOfWeek.atStartOfDay(),
                endOfWeek.plusDays(1).atStartOfDay()
        )).thenReturn(List.of()); // No completed sessions

        // When
        notificationSchedulerService.sendMissedSessionAlerts();

        // Then
        verify(notificationService).notifyCoach("system", any(String.class));
    }

    @Test
    void sendFatigueWarnings_shouldSendWarningWhenLowReadinessStreak() {
        // Given
        String athleteId = "athlete-123";
        Athlete athlete = new Athlete(athleteId, "Test Athlete", null, null, null, null);

        LocalDate threeDaysAgo = LocalDate.now().minusDays(3);
        List<WellnessSnapshot> lowReadinessData = List.of(
                new WellnessSnapshot(athleteId, threeDaysAgo, 35.0, null, null),
                new WellnessSnapshot(athleteId, threeDaysAgo.plusDays(1), 38.0, null, null),
                new WellnessSnapshot(athleteId, threeDaysAgo.plusDays(2), 32.0, null, null)
        );

        when(athleteRepository.findAll()).thenReturn(List.of(athlete));
        when(wellnessRepository.findByAthleteId(athleteId)).thenReturn(lowReadinessData);

        // When
        notificationSchedulerService.sendFatigueWarnings();

        // Then
        verify(notificationService, times(2)).notifyAthlete(athleteId, any(String.class));
        verify(notificationService, times(1)).notifyCoach("system", any(String.class));
    }

    private Plan createMockPlan() {
        LocalDate today = LocalDate.now();
        Workout workout = new Workout("workout-1", today, "ENDURANCE", 90, "moderate");
        return new Plan("plan-1", "athlete-123", List.of(workout), null, null, null, null, null, null, null);
    }
}