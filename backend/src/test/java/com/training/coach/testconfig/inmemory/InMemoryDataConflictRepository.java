package com.training.coach.testconfig.inmemory;

import com.training.coach.reconciliation.application.port.out.DataConflictRepository;
import com.training.coach.reconciliation.domain.model.DataConflict;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory DataConflictRepository for fast tests.
 */
public class InMemoryDataConflictRepository implements DataConflictRepository {
    private final ConcurrentHashMap<String, DataConflict> conflicts = new ConcurrentHashMap<>();

    @Override
    public DataConflict save(DataConflict conflict) {
        conflicts.put(conflict.id(), conflict);
        return conflict;
    }

    @Override
    public Optional<DataConflict> findById(String conflictId) {
        return Optional.ofNullable(conflicts.get(conflictId));
    }

    @Override
    public List<DataConflict> findByAthleteId(String athleteId) {
        return conflicts.values().stream()
                .filter(c -> c.athleteId().equals(athleteId))
                .collect(Collectors.toList());
    }

    @Override
    public List<DataConflict> findUnresolvedByAthleteId(String athleteId) {
        return conflicts.values().stream()
                .filter(c -> c.athleteId().equals(athleteId) && !c.isResolved())
                .collect(Collectors.toList());
    }

    @Override
    public List<DataConflict> findRequiresReview() {
        return conflicts.values().stream()
                .filter(DataConflict::requiresManualReview)
                .collect(Collectors.toList());
    }

    @Override
    public List<DataConflict> findByDate(String athleteId, LocalDateTime date) {
        return conflicts.values().stream()
                .filter(c -> c.athleteId().equals(athleteId))
                .filter(c -> c.activityDate().toLocalDate().equals(date.toLocalDate()))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String conflictId) {
        conflicts.remove(conflictId);
    }

    /**
     * Clear all conflicts for testing purposes.
     */
    public void clearAll() {
        conflicts.clear();
    }
}