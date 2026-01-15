package com.training.coach.sync.application.service;

import com.training.coach.athlete.application.port.out.FitnessPlatformPort;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Application service for syncing athlete data from fitness platforms.
 */
@Service
public class SyncService {

    private static final Logger logger = LoggerFactory.getLogger(SyncService.class);

    private final FitnessPlatformPort fitnessPlatformPort;

    public SyncService(FitnessPlatformPort fitnessPlatformPort) {
        this.fitnessPlatformPort = fitnessPlatformPort;
    }

    public void syncAthleteData(String athleteId, LocalDate startDate, LocalDate endDate) {
        // Sync activities
        var activitiesResult = fitnessPlatformPort.getActivities(athleteId, startDate, endDate);
        if (activitiesResult.isSuccess()) {
            var activities = activitiesResult.value().get();
            // Process and store activities (e.g., upsert to database)
            // For MVP, just log
            logger.info("Synced {} activities for athlete {}", activities.size(), athleteId);
        }

        // Sync wellness data (for latest date)
        var wellnessResult = fitnessPlatformPort.getWellnessData(athleteId, endDate);
        if (wellnessResult.isSuccess()) {
            var wellness = wellnessResult.value().get();
            // Process and store wellness data
            logger.info("Synced wellness data for athlete {} on {}: {}", athleteId, endDate, wellness);
        }
    }
}
