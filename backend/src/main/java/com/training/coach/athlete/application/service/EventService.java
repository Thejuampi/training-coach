package com.training.coach.athlete.application.service;

import com.training.coach.athlete.application.port.out.EventRepository;
import com.training.coach.athlete.domain.model.Event;
import com.training.coach.shared.functional.Result;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

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
}