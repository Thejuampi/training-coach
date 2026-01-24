package com.training.coach.athlete.presentation;

import com.training.coach.athlete.application.service.EventService;
import com.training.coach.athlete.domain.model.Event;
import com.training.coach.athlete.domain.model.TaperPeriod;
import com.training.coach.shared.functional.Result;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for event management operations.
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Add a new goal event for an athlete.
     */
    @PostMapping("/{athleteId}")
    public ResponseEntity<Event> addEvent(
            @PathVariable String athleteId,
            @RequestParam String name,
            @RequestParam LocalDate date,
            @RequestParam Event.EventPriority priority
    ) {
        Result<Event> result = eventService.addEvent(athleteId, name, date, priority);
        return ResponseEntity.ok(result.value().orElseThrow());
    }

    /**
     * Get all events for an athlete.
     */
    @GetMapping("/{athleteId}")
    public ResponseEntity<List<Event>> getEvents(@PathVariable String athleteId) {
        List<Event> events = eventService.getEvents(athleteId);
        return ResponseEntity.ok(events);
    }

    /**
     * Get upcoming events for an athlete after a specific date.
     */
    @GetMapping("/{athleteId}/upcoming")
    public ResponseEntity<List<Event>> getUpcomingEvents(
            @PathVariable String athleteId,
            @RequestParam LocalDate afterDate
    ) {
        List<Event> events = eventService.getUpcomingEvents(athleteId, afterDate);
        return ResponseEntity.ok(events);
    }

    /**
     * Update event date (triggers plan rebase if applicable).
     */
    @PutMapping("/{eventId}/date")
    public ResponseEntity<Event> updateEventDate(
            @PathVariable String eventId,
            @RequestParam LocalDate newDate
    ) {
        Result<Event> result = eventService.updateEventDate(eventId, newDate);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result.value().orElseThrow());
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Calculate taper period for an event.
     */
    @GetMapping("/{eventId}/taper")
    public ResponseEntity<TaperPeriod> calculateTaper(@PathVariable String eventId) {
        return eventService.getEvent(eventId)
            .map(event -> {
                TaperPeriod taper = eventService.calculateTaper(event);
                return ResponseEntity.ok(taper);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get A-priority events requiring taper.
     */
    @GetMapping("/{athleteId}/priority-a")
    public ResponseEntity<List<Event>> getAPriorityEvents(@PathVariable String athleteId) {
        List<Event> events = eventService.getAPriorityEvents(athleteId);
        return ResponseEntity.ok(events);
    }
}