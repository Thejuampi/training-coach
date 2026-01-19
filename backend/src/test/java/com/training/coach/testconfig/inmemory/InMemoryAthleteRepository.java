package com.training.coach.testconfig.inmemory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.training.coach.athlete.application.port.out.AthleteRepository;
import com.training.coach.athlete.domain.model.Athlete;

/**
 * In-memory AthleteRepository for fast tests.
 */
public class InMemoryAthleteRepository implements AthleteRepository {
    private final ConcurrentHashMap<String, Athlete> athletes = new ConcurrentHashMap<>();

    @Override
    public Athlete save(Athlete athlete) {
        athletes.put(athlete.id(), athlete);
        return athlete;
    }

    @Override
    public Optional<Athlete> findById(String id) {
        return Optional.ofNullable(athletes.get(id));
    }

    @Override
    public void deleteById(String id) {
        athletes.remove(id);
    }

    @Override
    public List<Athlete> findAll() {
        return List.copyOf(athletes.values());
    }
}
