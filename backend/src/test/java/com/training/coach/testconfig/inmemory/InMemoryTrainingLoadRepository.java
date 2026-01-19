package com.training.coach.testconfig.inmemory;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.training.coach.wellness.application.port.out.TrainingLoadRepository;
import com.training.coach.wellness.domain.model.TrainingLoadSummary;

/**
 * In-memory TrainingLoadRepository for fast tests.
 */
public class InMemoryTrainingLoadRepository implements TrainingLoadRepository {
    private final ConcurrentHashMap<String, java.util.Map<LocalDate, Double>> ctl = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, java.util.Map<LocalDate, Double>> atl = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, java.util.Map<LocalDate, TrainingLoadSummary>> summaries = new ConcurrentHashMap<>();

    @Override
    public Optional<TrainingLoadSummary> findTrainingLoadByAthleteIdAndDate(String athleteId, LocalDate date) {
        return Optional.ofNullable(summaries.getOrDefault(athleteId, java.util.Map.of()).get(date));
    }

    @Override
    public double findCtlByAthleteIdAndDate(String athleteId, LocalDate date) {
        return ctl.getOrDefault(athleteId, java.util.Map.of()).getOrDefault(date, 0.0);
    }

    @Override
    public double findAtlByAthleteIdAndDate(String athleteId, LocalDate date) {
        return atl.getOrDefault(athleteId, java.util.Map.of()).getOrDefault(date, 0.0);
    }

    @Override
    public void saveCtl(String athleteId, LocalDate date, double ctlValue) {
        ctl.computeIfAbsent(athleteId, key -> new java.util.HashMap<>()).put(date, ctlValue);
        summaries.computeIfAbsent(athleteId, key -> new java.util.HashMap<>())
                .computeIfAbsent(date, key -> TrainingLoadSummary.empty());
    }

    @Override
    public void saveAtl(String athleteId, LocalDate date, double atlValue) {
        atl.computeIfAbsent(athleteId, key -> new java.util.HashMap<>()).put(date, atlValue);
        summaries.computeIfAbsent(athleteId, key -> new java.util.HashMap<>())
                .computeIfAbsent(date, key -> TrainingLoadSummary.empty());
    }
}
