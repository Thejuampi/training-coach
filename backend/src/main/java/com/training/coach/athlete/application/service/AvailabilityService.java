package com.training.coach.athlete.application.service;

import com.training.coach.athlete.application.port.out.AvailabilityTemplateRepository;
import com.training.coach.athlete.application.port.out.TravelExceptionRepository;
import com.training.coach.athlete.application.port.out.PlanRepository;
import com.training.coach.athlete.domain.model.AvailabilityTemplate;
import com.training.coach.athlete.domain.model.TravelException;
import com.training.coach.shared.domain.unit.Hours;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for managing athlete availability templates and exceptions.
 */
@Service
public class AvailabilityService {

    private static final Logger logger = LoggerFactory.getLogger(AvailabilityService.class);

    private final AvailabilityTemplateRepository templateRepository;
    private final TravelExceptionRepository exceptionRepository;
    private final PlanRepository planRepository;

    public AvailabilityService(
            AvailabilityTemplateRepository templateRepository,
            TravelExceptionRepository exceptionRepository,
            PlanRepository planRepository) {
        this.templateRepository = templateRepository;
        this.exceptionRepository = exceptionRepository;
        this.planRepository = planRepository;
    }

    /**
     * Create a new availability template.
     */
    public AvailabilityTemplate createTemplate(
            String athleteId,
            Set<DayOfWeek> availableDays,
            Hours weeklyTargetHours,
            String name
    ) {
        AvailabilityTemplate template = AvailabilityTemplate.create(
                athleteId,
                availableDays,
                weeklyTargetHours,
                name
        );

        // Deactivate any existing active template
        templateRepository.findActiveByAthleteId(athleteId)
                .ifPresent(active -> {
                    AvailabilityTemplate deactivated = active.deactivate();
                    templateRepository.save(deactivated);
                });

        AvailabilityTemplate saved = templateRepository.save(template);
        logger.info("Created availability template '{}' for athlete {}", name, athleteId);
        return saved;
    }

    /**
     * Update an availability template.
     */
    public AvailabilityTemplate updateTemplate(
            String templateId,
            Set<DayOfWeek> availableDays,
            Hours weeklyTargetHours
    ) {
        AvailabilityTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        AvailabilityTemplate updated = template.update(availableDays, weeklyTargetHours);
        AvailabilityTemplate saved = templateRepository.save(updated);
        logger.info("Updated availability template {}", templateId);
        return saved;
    }

    /**
     * Get the active availability template for an athlete.
     */
    public AvailabilityTemplate getActiveTemplate(String athleteId) {
        return templateRepository.findActiveByAthleteId(athleteId)
                .orElseThrow(() -> new IllegalArgumentException("No active template found for athlete: " + athleteId));
    }

    /**
     * Add a travel exception.
     */
    public TravelException addException(
            String athleteId,
            LocalDate startDate,
            LocalDate endDate,
            TravelException.ExceptionType type,
            String description
    ) {
        TravelException exception = TravelException.create(
                athleteId,
                startDate,
                endDate,
                type,
                description
        );

        TravelException saved = exceptionRepository.save(exception);
        logger.info("Added {} exception for athlete {} from {} to {}",
                type, athleteId, startDate, endDate);
        return saved;
    }

    /**
     * Find conflicting workouts for a date range.
     */
    public List<LocalDate> findConflictingWorkouts(
            String athleteId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<LocalDate> conflicts = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            List<TravelException> exceptions = exceptionRepository.findCoveringDate(athleteId, current);
            if (!exceptions.isEmpty()) {
                conflicts.add(current);
            }
            current = current.plusDays(1);
        }
        return conflicts;
    }

    /**
     * Auto-reschedule workouts from exception dates to available dates.
     */
    public TravelAvailabilityService.RescheduleResult autoReschedule(
            String athleteId,
            LocalDate exceptionStart,
            LocalDate exceptionEnd
    ) {
        // Find the week containing the exception period
        LocalDate weekStart = exceptionStart.minusDays(exceptionStart.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);

        List<LocalDate> conflictingDates = findConflictingWorkouts(athleteId, exceptionStart, exceptionEnd);
        Set<LocalDate> exceptionDates = new HashSet<>(conflictingDates);

        // Get athlete's availability
        AvailabilityTemplate template = getActiveTemplate(athleteId);
        Set<DayOfWeek> availableDays = template.availableDays();

        // Find available dates in the same week (excluding exception dates)
        List<LocalDate> availableDates = new ArrayList<>();
        LocalDate current = weekStart;
        while (!current.isAfter(weekEnd)) {
            if (!exceptionDates.contains(current) && availableDays.contains(current.getDayOfWeek())) {
                availableDates.add(current);
            }
            current = current.plusDays(1);
        }

        // Move conflicting workouts to available dates
        List<LocalDate> rescheduledDates = new ArrayList<>();
        int moves = Math.min(conflictingDates.size(), availableDates.size());
        for (int i = 0; i < moves; i++) {
            rescheduledDates.add(availableDates.get(i));
        }

        boolean volumeMaintained = rescheduledDates.size() >= conflictingDates.size();
        boolean success = moves > 0 || conflictingDates.isEmpty();

        return new TravelAvailabilityService.RescheduleResult(
                conflictingDates.size(),
                rescheduledDates.size(),
                success,
                rescheduledDates,
                volumeMaintained
        );
    }

    /**
     * Resolve a travel exception.
     */
    public TravelException resolveException(String exceptionId) {
        TravelException exception = exceptionRepository.findById(exceptionId)
                .orElseThrow(() -> new IllegalArgumentException("Exception not found: " + exceptionId));

        TravelException resolved = exception.resolve();
        TravelException saved = exceptionRepository.save(resolved);
        logger.info("Resolved travel exception {}", exceptionId);
        return saved;
    }

    /**
     * Cancel a travel exception.
     */
    public TravelException cancelException(String exceptionId) {
        TravelException exception = exceptionRepository.findById(exceptionId)
                .orElseThrow(() -> new IllegalArgumentException("Exception not found: " + exceptionId));

        TravelException cancelled = exception.cancel();
        TravelException saved = exceptionRepository.save(cancelled);
        logger.info("Cancelled travel exception {}", exceptionId);
        return saved;
    }

    /**
     * Get all exceptions for an athlete.
     */
    public List<TravelException> getExceptions(String athleteId) {
        return exceptionRepository.findByAthleteId(athleteId);
    }

    /**
     * Get active exceptions for an athlete.
     */
    public List<TravelException> getActiveExceptions(String athleteId) {
        return exceptionRepository.findActiveByAthleteId(athleteId);
    }

    /**
     * Check if a date is available for training.
     */
    public boolean isAvailable(String athleteId, LocalDate date) {
        // Check for exceptions first
        List<TravelException> exceptions = exceptionRepository.findCoveringDate(athleteId, date);
        if (!exceptions.isEmpty()) {
            return false;
        }

        // Check availability template
        return templateRepository.findActiveByAthleteId(athleteId)
                .map(template -> template.isAvailable(date.getDayOfWeek()))
                .orElse(true); // Default to available if no template
    }
}