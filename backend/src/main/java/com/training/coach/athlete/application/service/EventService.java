package com.training.coach.athlete.application.service;

import com.training.coach.athlete.application.port.out.EventRepository;
import com.training.coach.athlete.application.port.out.PlanRepository;
import com.training.coach.athlete.domain.model.Event;
import com.training.coach.athlete.domain.model.TaperPeriod;
import com.training.coach.shared.functional.Result;
import com.training.coach.trainingplan.application.service.PlanService;
import com.training.coach.trainingplan.domain.model.PlanRebase;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for managing athlete events (e.g., competitions, important dates).
 * Integrates with plan rebasing when event dates change.
 */
@Service
public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    private final EventRepository repository;
    private final PlanService planService;
    private final PlanRebaseService planRebaseService;

    public EventService(
            EventRepository repository,
            PlanService planService,
            PlanRebaseService planRebaseService) {
        this.repository = repository;
        this.planService = planService;
        this.planRebaseService = planRebaseService;
    }

    /**
     * Add a new goal event for an athlete.
     */
    public Result<Event> addEvent(String athleteId, String name, LocalDate date, Event.EventPriority priority) {
        Event event = Event.create(athleteId, name, date, priority);
        Event saved = repository.save(event);
        logger.info("Added event '{}' for athlete {} on {}", name, athleteId, date);
        return Result.success(saved);
    }

    /**
     * Get all events for an athlete.
     */
    public List<Event> getEvents(String athleteId) {
        return repository.findByAthleteId(athleteId);
    }

    /**
     * Get a specific event by ID.
     */
    public Optional<Event> getEvent(String eventId) {
        return Optional.ofNullable(repository.findById(eventId));
    }

    /**
     * Update event date and trigger plan rebase if needed.
     * For A-priority events, rebases the athlete's active training plan.
     */
    public Result<Event> updateEventDate(String eventId, LocalDate newDate) {
        return Optional.ofNullable(repository.findById(eventId))
            .map(existing -> {
                LocalDate oldDate = existing.date();
                Event updated = existing.withDate(newDate);
                Event saved = repository.save(updated);

                // Trigger plan rebase for A-priority events
                if (existing.priority() == Event.EventPriority.A) {
                    rebasePlanForEventChange(saved.athleteId(), oldDate, newDate);
                }

                logger.info("Event date changed from {} to {} for event {}", oldDate, newDate, eventId);

                return Result.success(saved);
            })
            .orElse(Result.failure(new RuntimeException("Event not found")));
    }

    /**
     * Rebase training plans when an A-priority event date changes.
     * Finds plans where the event is the target (end) event and rebases them.
     */
    private void rebasePlanForEventChange(String athleteId, LocalDate oldDate, LocalDate newDate) {
        try {
            var plans = planService.getPlansForAthlete(athleteId);
            if (plans.isEmpty()) {
                logger.info("No published plans found for athlete {}, skipping rebase", athleteId);
                return;
            }

            // Calculate the date shift
            long daysShift = java.time.temporal.ChronoUnit.DAYS.between(oldDate, newDate);
            if (daysShift == 0) {
                return;
            }

            // Rebase each published plan where the event is near the plan end
            for (var planSummary : plans) {
                var planVersion = planService.getPlanVersion(planSummary.id(), planSummary.currentVersion());

                // Get the plan's end date (last workout date)
                LocalDate planEndDate = planVersion.workouts().stream()
                    .map(workout -> workout.date())
                    .max(java.time.Comparator.naturalOrder())
                    .orElse(null);

                if (planEndDate == null) {
                    continue;
                }

                // Check if the event is within 7 days of the plan end (likely the goal event)
                long daysFromPlanEnd = Math.abs(java.time.temporal.ChronoUnit.DAYS.between(oldDate, planEndDate));
                if (daysFromPlanEnd <= 7) {
                    // The event is likely the goal event for this plan - rebase it
                    LocalDate newPlanEndDate = planEndDate.plusDays((int) daysShift);

                    planRebaseService.rebasePlanToDate(
                        planSummary.id(),
                        planSummary.currentVersion(),
                        newPlanEndDate,
                        String.format("Event date changed from %s to %s", oldDate, newDate),
                        "system"
                    );

                    logger.info("Rebased plan {} for athlete {} due to A-priority event date change ({} day shift)",
                        planSummary.id(), athleteId, daysShift);
                }
            }
        } catch (Exception e) {
            // Log but don't fail the event update
            logger.error("Failed to rebase plan for event date change: {}", e.getMessage(), e);
        }
    }

    /**
     * Calculate taper period for an event.
     */
    public TaperPeriod calculateTaper(Event event) {
        return TaperPeriod.createForEvent(event);
    }

    /**
     * Get all A-priority events for an athlete (events requiring taper).
     */
    public List<Event> getAPriorityEvents(String athleteId) {
        return repository.findByAthleteId(athleteId).stream()
            .filter(event -> event.priority() == Event.EventPriority.A)
            .toList();
    }

    /**
     * Get upcoming events for an athlete after a specific date.
     */
    public List<Event> getUpcomingEvents(String athleteId, LocalDate afterDate) {
        return repository.findByAthleteId(athleteId).stream()
            .filter(event -> event.date().isAfter(afterDate))
            .sorted(java.util.Comparator.comparing(Event::date))
            .toList();
    }
}
