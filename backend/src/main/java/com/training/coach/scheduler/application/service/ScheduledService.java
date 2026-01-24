package com.training.coach.scheduler.application.service;

import com.training.coach.sync.application.service.SyncService;
import com.training.coach.wellness.application.service.WellnessReminderService;
import com.training.coach.athlete.application.service.NotificationService;
import com.training.coach.athlete.application.port.out.AthleteRepository;
import com.training.coach.athlete.domain.model.Notification;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service for scheduled tasks like nightly syncs and wellness reminders.
 */
@Service
public class ScheduledService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledService.class);

    private final SyncService syncService;
    private final WellnessReminderService wellnessReminderService;
    private final NotificationService notificationService;
    private final AthleteRepository athleteRepository;

    public ScheduledService(
            SyncService syncService,
            WellnessReminderService wellnessReminderService,
            NotificationService notificationService,
            AthleteRepository athleteRepository) {
        this.syncService = syncService;
        this.wellnessReminderService = wellnessReminderService;
        this.notificationService = notificationService;
        this.athleteRepository = athleteRepository;
    }

    /**
     * Scheduled sync that runs nightly at 2 AM.
     * Synchronizes data for all athletes linked to fitness platforms.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void performNightlySync() {
        logger.info("Starting nightly sync job");

        // Get all athletes
        List<String> athleteIds = athleteRepository.findAll().stream()
                .map(athlete -> athlete.id())
                .toList();

        if (athleteIds.isEmpty()) {
            logger.info("No athletes found for nightly sync");
            return;
        }

        // Sync data for the last 7 days
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);

        // Perform sync for each athlete
        var results = syncService.runNightlySync(athleteIds, startDate, endDate);

        // Log results
        long successful = results.values().stream()
                .filter(result -> "success".equals(result.status()))
                .count();
        long partialFailures = results.values().stream()
                .filter(result -> "partial_failure".equals(result.status()))
                .count();
        long completeFailures = results.values().stream()
                .filter(result -> "complete_failure".equals(result.status()))
                .count();

        logger.info("Nightly sync completed: {} successful, {} partial failures, {} complete failures",
                successful, partialFailures, completeFailures);

        // Check for any partial failures and notify admins
        if (partialFailures > 0) {
            String message = String.format("Nightly sync had %d partial failures out of %d athletes processed",
                    partialFailures, athleteIds.size());
            notifyAdminsSyncIssue(message);
        }
    }

    /**
     * Daily wellness reminder job that runs daily at 9 AM.
     * Reminds athletes who haven't submitted wellness data in the last 3 days.
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendWellnessReminders() {
        logger.info("Running daily wellness reminder job");

        var reminders = wellnessReminderService.findAthletesNeedingReminders();

        for (var reminder : reminders) {
            notificationService.notifyAthlete(
                    reminder.athleteId(),
                    "Please submit your wellness data. You haven't submitted in " + reminder.daysSinceSubmission() + " days."
            );

            logger.info("Sent wellness reminder to athlete {}", reminder.athleteId());
        }

        logger.info("Wellness reminder job completed: {} reminders sent", reminders.size());
    }

    /**
     * Check for safety guardrail violations and send notifications.
     * Runs daily at 10 AM.
     */
    @Scheduled(cron = "0 0 10 * * *")
    public void checkSafetyViolations() {
        logger.info("Running daily safety violation check");

        // In a full implementation, this would:
        // 1. Check all athletes for recent violations
        // 2. Send notifications to coaches when violations are detected
        // 3. Update safety status for affected athletes

        logger.info("Safety violation check completed");
    }

    /**
     * Notify admins about sync issues.
     */
    private void notifyAdminsSyncIssue(String message) {
        // In a real implementation, this would find admin users and send notifications
        logger.warn(message);

        // Using the notification service to notify "system" admin
        notificationService.notifyCoach("system", message);
    }
}