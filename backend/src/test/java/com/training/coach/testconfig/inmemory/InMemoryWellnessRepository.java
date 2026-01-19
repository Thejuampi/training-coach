package com.training.coach.testconfig.inmemory;

import com.training.coach.wellness.application.port.out.WellnessRepository;
import com.training.coach.wellness.domain.model.WellnessSnapshot;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory WellnessRepository for fast tests.
 */
public class InMemoryWellnessRepository implements WellnessRepository {
    private final ConcurrentHashMap<String, java.util.List<WellnessSnapshot>> byAthlete = new ConcurrentHashMap<>();

    @Override
    public Optional<WellnessSnapshot> findByAthleteIdAndDate(String athleteId, LocalDate date) {
        return Optional.ofNullable(byAthlete.get(athleteId))
                .flatMap(list -> list.stream().filter(snapshot -> snapshot.date().equals(date)).findFirst());
    }

    @Override
    public List<WellnessSnapshot> findByAthleteIdAndDateRange(String athleteId, LocalDate startDate, LocalDate endDate) {
        List<WellnessSnapshot> stored = byAthlete.getOrDefault(athleteId, List.of());
        return stored.stream()
                .filter(snapshot -> !snapshot.date().isBefore(startDate) && !snapshot.date().isAfter(endDate))
                .toList();
    }

    @Override
    public Optional<WellnessSnapshot> findLatestByAthleteId(String athleteId) {
        List<WellnessSnapshot> stored = byAthlete.getOrDefault(athleteId, List.of());
        return stored.stream().max(java.util.Comparator.comparing(WellnessSnapshot::date));
    }

    @Override
    public WellnessSnapshot save(WellnessSnapshot snapshot) {
        byAthlete.computeIfAbsent(snapshot.athleteId(), key -> new java.util.ArrayList<>())
                .removeIf(existing -> existing.date().equals(snapshot.date()));
        byAthlete.get(snapshot.athleteId()).add(snapshot);
        return snapshot;
    }

    @Override
    public List<WellnessSnapshot> saveAll(List<WellnessSnapshot> snapshots) {
        snapshots.forEach(this::save);
        return snapshots;
    }

    @Override
    public void deleteByAthleteIdAndDate(String athleteId, LocalDate date) {
        List<WellnessSnapshot> stored = byAthlete.get(athleteId);
        if (stored != null) {
            stored.removeIf(snapshot -> snapshot.date().equals(date));
        }
    }

    @Override
    public boolean existsByAthleteIdAndDate(String athleteId, LocalDate date) {
        return byAthlete.getOrDefault(athleteId, List.of()).stream().anyMatch(snapshot -> snapshot.date().equals(date));
    }
}
