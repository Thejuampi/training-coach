package com.training.coach.testconfig.inmemory;

import com.training.coach.athlete.application.port.out.AvailabilityTemplateRepository;
import com.training.coach.athlete.domain.model.AvailabilityTemplate;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory AvailabilityTemplateRepository for fast tests.
 */
public class InMemoryAvailabilityTemplateRepository implements AvailabilityTemplateRepository {
    private final ConcurrentHashMap<String, AvailabilityTemplate> templates = new ConcurrentHashMap<>();

    @Override
    public AvailabilityTemplate save(AvailabilityTemplate template) {
        templates.put(template.id(), template);
        return template;
    }

    @Override
    public Optional<AvailabilityTemplate> findById(String templateId) {
        return Optional.ofNullable(templates.get(templateId));
    }

    @Override
    public List<AvailabilityTemplate> findByAthleteId(String athleteId) {
        return templates.values().stream()
                .filter(t -> t.athleteId().equals(athleteId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AvailabilityTemplate> findActiveByAthleteId(String athleteId) {
        return templates.values().stream()
                .filter(t -> t.athleteId().equals(athleteId) && t.isActive())
                .findFirst();
    }

    @Override
    public void delete(String templateId) {
        templates.remove(templateId);
    }

    /**
     * Clear all templates for testing purposes.
     */
    public void clearAll() {
        templates.clear();
    }
}