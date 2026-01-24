package com.training.coach.wellness.application.service;

import com.training.coach.athlete.application.port.out.AthleteRepository;
import com.training.coach.athlete.domain.model.Athlete;
import com.training.coach.wellness.application.port.out.WellnessRepository;
import com.training.coach.wellness.domain.model.WellnessSnapshot;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Service for managing wellness reminders.
 */
@Service
public class WellnessReminderService {

    private final AthleteRepository athleteRepository;
    private final WellnessRepository wellnessRepository;

    public WellnessReminderService(AthleteRepository athleteRepository, WellnessRepository wellnessRepository) {
        this.athleteRepository = athleteRepository;
        this.wellnessRepository = wellnessRepository;
    }

    /**
     * Find athletes who need wellness reminders (haven't submitted in 3+ days).
     */
    public List<AthleteWellnessReminder> findAthletesNeedingReminders() {
        LocalDate threeDaysAgo = LocalDate.now().minusDays(3);

        return athleteRepository.findAll().stream()
                .filter(athlete -> needsWellnessReminder(athlete.id(), threeDaysAgo))
                .map(athlete -> new AthleteWellnessReminder(
                        athlete.id(),
                        athlete.name(),
                        calculateDaysSinceSubmission(athlete.id(), threeDaysAgo)
                ))
                .collect(Collectors.toList());
    }

    /**
     * Check if an athlete needs a wellness reminder.
     */
    private boolean needsWellnessReminder(String athleteId, LocalDate threeDaysAgo) {
        // Get latest wellness submission for the athlete
        var latestWellness = wellnessRepository.findByAthleteId(athleteId)
                .stream()
                .max((w1, w2) -> w1.date().compareTo(w2.date()));

        if (latestWellness.isEmpty()) {
            // No wellness data ever - remind to start submitting
            return true;
        }

        // Check if last submission was more than 3 days ago
        LocalDate lastSubmission = latestWellness.get().date();
        long daysSince = ChronoUnit.DAYS.between(lastSubmission, LocalDate.now());

        return daysSince >= 3;
    }

    /**
     * Calculate how many days since the last wellness submission.
     */
    private int calculateDaysSinceSubmission(String athleteId, LocalDate threeDaysAgo) {
        var latestWellness = wellnessRepository.findByAthleteId(athleteId)
                .stream()
                .max((w1, w2) -> w1.date().compareTo(w2.date()));

        if (latestWellness.isEmpty()) {
            return -1; // Never submitted
        }

        LocalDate lastSubmission = latestWellness.get().date();
        return (int) ChronoUnit.DAYS.between(lastSubmission, LocalDate.now());
    }

    /**
     * Record that a wellness reminder was sent.
     */
    public void recordReminderSent(String athleteId) {
        // In a real implementation, this would update a reminder log
        // For now, we'll just log it
        System.out.println("Wellness reminder sent to athlete " + athleteId);
    }

    /**
     * Record that a wellness submission was made.
     */
    public void recordSubmissionMade(String athleteId) {
        // In a real implementation, this would reset the reminder counter
        // For now, we'll just log it
        System.out.println("Wellness submission recorded for athlete " + athleteId);
    }

    /**
     * Record for athlete wellness reminder.
     */
    public record AthleteWellnessReminder(
            String athleteId,
            String athleteName,
            int daysSinceSubmission
    ) {}
}