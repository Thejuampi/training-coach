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
     */
    public Result<Event> updateEventDate(String eventId, LocalDate newDate) {
        return repository.findById(eventId)
            .map(existing -> {
                LocalDate oldDate = existing.date();
                Event updated = existing.withDate(newDate);
                Event saved = repository.save(updated);

                // Check if there's a published plan that needs rebasing
                planService.getPlansForAthlete(existing.athleteId()).stream()
                    .filter(plan -> plan.endDate().equals(oldDate))
                    .findFirst()
                    .ifPresent(plan -> {
                        logger.info("Triggering plan rebase for plan {} due to event date change", plan.id());
                        planRebaseService.rebasePlanToDate(
                            plan.id(),
                            plan.currentVersion(),
                            newDate,
                            "Event date changed from " + oldDate + " to " + newDate,
                            "system"
                        );
                    });

                return Result.success(saved);
            })
            .orElse(Result.failure(new RuntimeException("Event not found")));
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
