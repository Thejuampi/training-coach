package com.training.coach.testconfig.inmemory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.training.coach.activity.application.port.out.ActivityRepository;
import com.training.coach.activity.domain.model.ActivityLight;

/**
 * In-memory ActivityRepository for fast tests.
 */
public class InMemoryActivityRepository implements ActivityRepository {
    private final ConcurrentHashMap<String, List<ActivityLight>> byAthleteId = new ConcurrentHashMap<>();

    @Override
    public ActivityLight save(ActivityLight activity) {
        byAthleteId.computeIfAbsent(activity.athleteId(), key -> new java.util.ArrayList<>())
                .removeIf(existing -> existing.externalActivityId().equals(activity.externalActivityId()));
        byAthleteId.get(activity.athleteId()).add(activity);
        return activity;
    }

    @Override
    public List<ActivityLight> saveAll(List<ActivityLight> activities) {
        activities.forEach(this::save);
        return activities;
    }

    @Override
    public Optional<ActivityLight> findByAthleteIdAndExternalActivityId(String athleteId, String externalActivityId) {
        return Optional.ofNullable(byAthleteId.get(athleteId))
                .flatMap(list -> list.stream().filter(activity -> activity.externalActivityId().equals(externalActivityId)).findFirst());
    }

    @Override
    public Optional<ActivityLight> findByAthleteIdAndDate(String athleteId, LocalDate date) {
        return Optional.ofNullable(byAthleteId.get(athleteId))
                .flatMap(list -> list.stream().filter(activity -> activity.date().equals(date)).findFirst());
    }

    @Override
    public List<ActivityLight> findByAthleteIdAndDateRange(String athleteId, LocalDate startDate, LocalDate endDate) {
        List<ActivityLight> stored = byAthleteId.getOrDefault(athleteId, List.of());
        return stored.stream()
                .filter(activity -> !activity.date().isBefore(startDate) && !activity.date().isAfter(endDate))
                .toList();
    }
}
