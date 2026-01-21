package com.training.coach.athlete.application.service;

import com.training.coach.athlete.application.port.out.EventRepository;
import com.training.coach.athlete.domain.model.Event;
import com.training.coach.shared.functional.Result;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Service for managing athlete events (e.g., competitions, important dates).
 */
@Service
public class EventService {

    private final EventRepository repository;

    public EventService(EventRepository repository) {
        this.repository = repository;
    }

    public Result<Event> addEvent(String athleteId, String name, LocalDate date, String priority) {
        String id = UUID.randomUUID().toString();
        Event event = new Event(id, athleteId, name, date, priority);
        return Result.success(repository.save(event));
    }

    public List<Event> getEvents(String athleteId) {
        return repository.findByAthleteId(athleteId);
    }

    public Result<Event> updateEventDate(String eventId, LocalDate newDate) {
        return repository.findById(eventId)
            .map(existing -> {
                Event updated = new Event(
                    existing.id(),
                    existing.athleteId(),
                    existing.name(),
                    newDate,
                    existing.priority()
                );
                return Result.success(repository.save(updated));
            })
            .orElse(Result.failure(new RuntimeException("Event not found")));
    }
}
