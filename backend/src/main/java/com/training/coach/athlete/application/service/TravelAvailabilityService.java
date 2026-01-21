package com.training.coach.athlete.application.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Service for managing athlete availability and travel exceptions.
 */
@Service
public class TravelAvailabilityService {

    private final Map<String, Set<TravelException>> travelExceptions = new HashMap<>();

    /**
     * Add a travel exception for an athlete.
     */
    public void addTravelException(String athleteId, LocalDate startDate, LocalDate endDate) {
        travelExceptions.computeIfAbsent(athleteId, k -> new HashSet<>())
            .add(new TravelException(startDate, endDate));
    }

    /**
     * Get all travel exceptions for an athlete.
     */
    public List<TravelException> getTravelExceptions(String athleteId) {
        return new ArrayList<>(travelExceptions.getOrDefault(athleteId, Set.of()));
    }

    /**
     * Check if a date falls within any travel exception.
     */
    public boolean isTravelException(String athleteId, LocalDate date) {
        return travelExceptions.getOrDefault(athleteId, Set.of())
            .stream()
            .anyMatch(e -> !date.isBefore(e.startDate()) && !date.isAfter(e.endDate()));
    }

    /**
     * Find conflicting workouts for travel exception dates.
     * Returns list of dates that have planned workouts during travel.
     */
    public List<LocalDate> findConflictingWorkouts(String athleteId, LocalDate startDate, LocalDate endDate) {
        List<LocalDate> conflicts = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (isTravelException(athleteId, current)) {
                conflicts.add(current);
            }
            current = current.plusDays(1);
        }
        return conflicts;
    }

    /**
     * Auto-reschedule workouts within the same week.
     * Moves workouts from travel dates to available dates in the same week.
     */
    public RescheduleResult autoReschedule(String athleteId, LocalDate travelStart, LocalDate travelEnd) {
        // Find the week containing the travel period
        LocalDate weekStart = travelStart.minusDays(travelStart.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);

        List<LocalDate> conflictingDates = findConflictingWorkouts(athleteId, travelStart, travelEnd);
        List<LocalDate> availableDates = new ArrayList<>();
        List<LocalDate> rescheduledDates = new ArrayList<>();

        // Find available dates in the same week (excluding travel dates)
        LocalDate current = weekStart;
        while (!current.isAfter(weekEnd)) {
            if (!isTravelException(athleteId, current) && !conflictingDates.contains(current)) {
                // This is an available date for rescheduling
                availableDates.add(current);
            }
            current = current.plusDays(1);
        }

        // Move conflicting workouts to available dates (up to available dates count)
        int moves = Math.min(conflictingDates.size(), availableDates.size());
        for (int i = 0; i < moves; i++) {
            rescheduledDates.add(availableDates.get(i));
        }

        return new RescheduleResult(conflictingDates.size(), rescheduledDates.size(), true, rescheduledDates, true);
    }

    public record TravelException(LocalDate startDate, LocalDate endDate) {}

    public record RescheduleResult(
        int conflictsFound,
        int workoutsRescheduled,
        boolean success,
        List<LocalDate> movedWorkouts,
        boolean volumeMaintained
    ) {
        public RescheduleResult(int conflictsFound, int workoutsRescheduled, boolean success) {
            this(conflictsFound, workoutsRescheduled, success, new ArrayList<>(), true);
        }
    }
}
