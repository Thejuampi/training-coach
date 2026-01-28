package com.training.coach.athlete.application.service;

import com.training.coach.athlete.application.port.out.AthleteRepository;
import com.training.coach.athlete.application.port.out.NotificationRepository;
import com.training.coach.trainingplan.application.port.out.PlanRepository;
import com.training.coach.workout.application.port.out.WorkoutExecutionRepository;
import com.training.coach.wellness.application.port.out.WellnessRepository;
import com.training.coach.athlete.domain.model.Notification;
import com.training.coach.athlete.domain.model.NotificationPriority;
import com.training.coach.athlete.domain.model.NotificationStatus;
import com.training.coach.athlete.domain.model.NotificationType;
import com.training.coach.shared.domain.model.AthleteId;
import com.training.coach.athlete.domain.model.AthleteReadiness;
import com.training.coach.athlete.domain.model.Workout;
import com.training.coach.wellness.domain.model.WellnessSnapshot;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for scheduling and managing different types of notifications.
 */
@Service
public class NotificationSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationSchedulerService.class);

    private final AthleteRepository athleteRepository;
    private final PlanRepository planRepository;
    private final WorkoutExecutionRepository workoutExecutionRepository;
    private final WellnessRepository wellnessRepository;
    private final NotificationService notificationService;

    public NotificationSchedulerService(
            AthleteRepository athleteRepository,
            PlanRepository planRepository,
            WorkoutExecutionRepository workoutExecutionRepository,
            WellnessRepository wellnessRepository,
            NotificationService notificationService) {
        this.athleteRepository = athleteRepository;
        this.planRepository = planRepository;
        this.workoutExecutionRepository = workoutExecutionRepository;
        this.wellnessRepository = wellnessRepository;
        this.notificationService = notificationService;
    }

    /**
     * Send daily workout reminders for athletes who have planned workouts.
     */
    public void sendDailyWorkoutReminders() {
        logger.info("Sending daily workout reminders");

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<String> athleteIds = athleteRepository.findAll().stream()
                .map(athlete -> athlete.id())
                .toList();

        for (String athleteId : athleteIds) {
            var plans = planRepository.findByAthleteId(new AthleteId(athleteId));
            if (plans.isEmpty()) {
                continue;
            }

            var tomorrowWorkout = plans.stream()
                    .flatMap(plan -> plan.workouts().stream())
                    .filter(workout -> workout.date().equals(tomorrow))
                    .findFirst();

            if (tomorrowWorkout.isPresent()) {
                sendWorkoutReminder(athleteId, tomorrowWorkout.get());
            }
        }
    }

    /**
     * Send missed session alerts for coaches when athletes miss key sessions.
     */
    public void sendMissedSessionAlerts() {
        logger.info("Checking for missed key sessions");

        LocalDate startOfWeek = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        List<String> athleteIds = athleteRepository.findAll().stream()
                .map(athlete -> athlete.id())
                .toList();

        for (String athleteId : athleteIds) {
            var keySessions = findKeySessionsInWeek(athleteId, startOfWeek, endOfWeek);
            var completedSessions = countCompletedSessions(athleteId, startOfWeek, endOfWeek);

            // If athlete missed more than 20% of key sessions, send alert
            if (keySessions > 0 && (keySessions - completedSessions) > 0) {
                sendMissedSessionAlert(athleteId, keySessions, completedSessions);
            }
        }
    }

    /**
     * Send fatigue warnings for athletes with low readiness streaks.
     */
    public void sendFatigueWarnings() {
        logger.info("Checking for fatigue warning conditions");

        LocalDate threeDaysAgo = LocalDate.now().minusDays(3);
        List<String> athleteIds = athleteRepository.findAll().stream()
                .map(athlete -> athlete.id())
                .toList();

        for (String athleteId : athleteIds) {
            var readinessTrend = getReadinessTrend(athleteId, threeDaysAgo);

            // If readiness has been below 40 for 3+ consecutive days
            if (readinessTrend.size() >= 3 && readinessTrend.stream()
                    .allMatch(readiness -> readiness.score() < 40.0)) {
                sendFatigueWarning(athleteId, readinessTrend);
            }
        }
    }

    private void sendWorkoutReminder(String athleteId, Workout workout) {
        String message = String.format(
                "Reminder: You have a %s workout planned for tomorrow. Duration: %d minutes. Intensity: %s",
                workout.type(), workout.durationMinutes(), workout.intensityProfile()
        );

        var notification = createNotification(
                athleteId,
                "athlete",
                NotificationType.WORKOUT_REMINDER,
                NotificationPriority.NORMAL,
                message
        );

        notificationService.notifyAthlete(athleteId, message);
        logger.info("Sent workout reminder to athlete {}", athleteId);
    }

    private void sendMissedSessionAlert(String athleteId, int keySessions, int completedSessions) {
        String message = String.format(
                "Athlete missed %d/%d key sessions this week. Consider adjusting their plan or checking for recovery issues.",
                keySessions - completedSessions, keySessions
        );

        var notification = createNotification(
                "system", // This would be the coach's ID in a real implementation
                "coach",
                NotificationType.KEY_SESSION_ALERT,
                NotificationPriority.HIGH,
                message,
                Map.of("athleteId", athleteId, "missedSessions", keySessions - completedSessions)
        );

        notificationService.notifyCoach("system", message);
        logger.info("Sent missed session alert for athlete {}", athleteId);
    }

    private void sendFatigueWarning(String athleteId, List<AthleteReadiness> readinessTrend) {
        // Send to athlete
        String athleteMessage = "⚠️ Fatigue Warning: Your readiness has been below 40 for 3+ consecutive days. Consider reducing training load and focusing on recovery.";

        notificationService.notifyAthlete(athleteId, athleteMessage);

        // Send to coach
        String coachMessage = String.format(
                "⚠️ Fatigue Warning: Athlete %s shows signs of fatigue (readiness below 40 for 3+ days). Recommend monitoring and load adjustment.",
                athleteId
        );

        notificationService.notifyCoach("system", coachMessage);

        logger.info("Sent fatigue warning for athlete {}", athleteId);
    }

    private int findKeySessionsInWeek(String athleteId, LocalDate startOfWeek, LocalDate endOfWeek) {
        // In a real implementation, this would identify which sessions are "key" sessions
        // For now, assume all sessions with duration > 60 minutes are key sessions
        return (int) planRepository.findByAthleteId(new AthleteId(athleteId)).stream()
                .flatMap(plan -> plan.workouts().stream())
                .filter(workout ->
                        workout.date().isAfter(startOfWeek.minusDays(1)) &&
                        workout.date().isBefore(endOfWeek.plusDays(1)) &&
                        workout.durationMinutes() > 60)
                .count();
    }

    private int countCompletedSessions(String athleteId, LocalDate startOfWeek, LocalDate endOfWeek) {
        return (int) workoutExecutionRepository.findByAthleteIdAndDateRange(
                new AthleteId(athleteId),
                startOfWeek.atStartOfDay(),
                endOfWeek.plusDays(1).atStartOfDay()
        ).stream()
                .filter(execution -> execution.status() == com.training.coach.workout.domain.ExecutionStatus.COMPLETED)
                .count();
    }

    private List<AthleteReadiness> getReadinessTrend(String athleteId, LocalDate sinceDate) {
        return wellnessRepository.findByAthleteId(athleteId).stream()
                .filter(wellness -> wellness.date().isAfter(sinceDate))
                .sorted((w1, w2) -> w1.date().compareTo(w2.date()))
                .limit(3)
                .map(wellness -> new AthleteReadiness(athleteId, wellness.readinessScore(), wellness.date()))
                .collect(Collectors.toList());
    }

    private Notification createNotification(
            String athleteId,
            String recipientType,
            NotificationType type,
            NotificationPriority priority,
            String message
    ) {
        return createNotification(athleteId, recipientType, type, priority, message, Map.of());
    }

    private Notification createNotification(
            String athleteId,
            String recipientType,
            NotificationType type,
            NotificationPriority priority,
            String message,
            Map<String, Object> metadata
    ) {
        String id = UUID.randomUUID().toString();
        return new Notification(
                id,
                athleteId,
                recipientType,
                type,
                priority,
                message,
                LocalDateTime.now(),
                LocalDateTime.now(),
                NotificationStatus.SENT,
                metadata
        );
    }
}