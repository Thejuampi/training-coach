package com.training.coach.analysis.application.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class SeilerThresholdService {

    private final Map<String, SeilerThresholds> thresholdsByAthleteId = new ConcurrentHashMap<>();

    public SeilerThresholds upsert(SeilerThresholds thresholds) {
        thresholdsByAthleteId.put(thresholds.athleteId(), thresholds);
        return thresholds;
    }

    public Optional<SeilerThresholds> getForAthlete(String athleteId) {
        return Optional.ofNullable(thresholdsByAthleteId.get(athleteId));
    }
}
