package com.training.coach.testconfig.inmemory;

import com.training.coach.privacy.application.port.out.ConsentLogRepository;
import com.training.coach.privacy.domain.model.ConsentLog;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory ConsentLogRepository for fast tests.
 */
public class InMemoryConsentLogRepository implements ConsentLogRepository {
    private final ConcurrentHashMap<String, ConsentLog> logs = new ConcurrentHashMap<>();

    @Override
    public ConsentLog save(ConsentLog log) {
        logs.put(log.id(), log);
        return log;
    }

    @Override
    public List<ConsentLog> findByAthleteId(String athleteId) {
        return logs.values().stream()
                .filter(l -> l.athleteId().equals(athleteId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ConsentLog> findByAthleteIdAndDateRange(String athleteId, Instant startDate, Instant endDate) {
        return logs.values().stream()
                .filter(l -> l.athleteId().equals(athleteId))
                .filter(l -> !l.timestamp().isBefore(startDate) && !l.timestamp().isAfter(endDate))
                .collect(Collectors.toList());
    }

    @Override
    public List<ConsentLog> findRecentByAthleteId(String athleteId, int limit) {
        return logs.values().stream()
                .filter(l -> l.athleteId().equals(athleteId))
                .sorted((a, b) -> b.timestamp().compareTo(a.timestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConsentLog> findByRequestId(String requestId) {
        return logs.values().stream()
                .filter(l -> l.requestId().equals(requestId))
                .collect(Collectors.toList());
    }

    @Override
    public int deleteByAthleteIdBeforeDate(String athleteId, Instant date) {
        int count = 0;
        for (ConsentLog log : logs.values()) {
            if (log.athleteId().equals(athleteId) && log.timestamp().isBefore(date)) {
                logs.remove(log.id());
                count++;
            }
        }
        return count;
    }

    /**
     * Clear all logs for testing purposes.
     */
    public void clearAll() {
        logs.clear();
    }
}
