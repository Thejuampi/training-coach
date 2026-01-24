package com.training.coach.testconfig.inmemory;

import com.training.coach.workout.application.port.out.WorkoutTemplateRepository;
import com.training.coach.workout.domain.model.WorkoutTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory WorkoutTemplateRepository for fast tests.
 */
public class InMemoryWorkoutTemplateRepository implements WorkoutTemplateRepository {
    private final ConcurrentHashMap<String, WorkoutTemplate> templates = new ConcurrentHashMap<>();

    @Override
    public WorkoutTemplate save(WorkoutTemplate template) {
        templates.put(template.id(), template);
        return template;
    }

    @Override
    public Optional<WorkoutTemplate> findById(String templateId) {
        return Optional.ofNullable(templates.get(templateId));
    }

    @Override
    public List<WorkoutTemplate> findByCoachId(String coachId) {
        return templates.values().stream()
                .filter(t -> t.coachId().equals(coachId))
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkoutTemplate> findAvailable() {
        return templates.values().stream()
                .filter(WorkoutTemplate::isAvailable)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkoutTemplate> findGloballyShared() {
        return templates.values().stream()
                .filter(WorkoutTemplate::isAvailable)
                .filter(WorkoutTemplate::sharedGlobally)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkoutTemplate> findByTags(Set<String> tags) {
        return templates.values().stream()
                .filter(t -> t.tags().stream().anyMatch(tags::contains))
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkoutTemplate> findByPhase(String phase) {
        return templates.values().stream()
                .filter(t -> t.phases().contains(phase))
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkoutTemplate> findByPurpose(String purpose) {
        return templates.values().stream()
                .filter(t -> t.purposes().contains(purpose))
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkoutTemplate> findVersionsByParentId(String parentTemplateId) {
        return templates.values().stream()
                .filter(t -> parentTemplateId.equals(t.parentTemplateId()))
                .sorted(Comparator.comparingInt(WorkoutTemplate::version).reversed())
                .collect(Collectors.toList());
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