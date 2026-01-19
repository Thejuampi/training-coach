package com.training.coach.testconfig.inmemory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.training.coach.athlete.application.port.out.EventRepository;
import com.training.coach.athlete.domain.model.Event;

/**
 * In-memory EventRepository for fast tests.
 */
public class InMemoryEventRepository implements EventRepository {
    private final ConcurrentHashMap<String, java.util.List<Event>> events = new ConcurrentHashMap<>();

    @Override
    public Event save(Event event) {
        events.computeIfAbsent(event.athleteId(), key -> new java.util.ArrayList<>()).add(event);
        return event;
    }

    @Override
    public List<Event> findByAthleteId(String athleteId) {
        return events.getOrDefault(athleteId, List.of());
    }
}
