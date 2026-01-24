package com.training.coach.testconfig.inmemory;

import com.training.coach.athlete.application.port.out.TravelExceptionRepository;
import com.training.coach.athlete.domain.model.TravelException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory TravelExceptionRepository for fast tests.
 */
public class InMemoryTravelExceptionRepository implements TravelExceptionRepository {
    private final ConcurrentHashMap<String, TravelException> exceptions = new ConcurrentHashMap<>();

    @Override
    public TravelException save(TravelException exception) {
        exceptions.put(exception.id(), exception);
        return exception;
    }

    @Override
    public Optional<TravelException> findById(String exceptionId) {
        return Optional.ofNullable(exceptions.get(exceptionId));
    }

    @Override
    public List<TravelException> findByAthleteId(String athleteId) {
        return exceptions.values().stream()
                .filter(e -> e.athleteId().equals(athleteId))
                .collect(Collectors.toList());
    }

    @Override
    public List<TravelException> findActiveByAthleteId(String athleteId) {
        return exceptions.values().stream()
                .filter(e -> e.athleteId().equals(athleteId) && e.status() == TravelException.ExceptionStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    @Override
    public List<TravelException> findOverlapping(String athleteId, LocalDate startDate, LocalDate endDate) {
        return exceptions.values().stream()
                .filter(e -> e.athleteId().equals(athleteId))
                .filter(e -> e.status() == TravelException.ExceptionStatus.ACTIVE)
                .filter(e -> !(e.endDate().isBefore(startDate) || e.startDate().isAfter(endDate)))
                .collect(Collectors.toList());
    }

    @Override
    public List<TravelException> findCoveringDate(String athleteId, LocalDate date) {
        return exceptions.values().stream()
                .filter(e -> e.athleteId().equals(athleteId))
                .filter(e -> e.status() == TravelException.ExceptionStatus.ACTIVE)
                .filter(e -> e.coversDate(date))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String exceptionId) {
        exceptions.remove(exceptionId);
    }

    /**
     * Clear all exceptions for testing purposes.
     */
    public void clearAll() {
        exceptions.clear();
    }
}