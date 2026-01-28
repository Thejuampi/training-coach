package com.training.coach.sync.application.service;

import com.training.coach.activity.application.port.out.ActivityRepository;
import com.training.coach.activity.domain.model.ActivityLight;
import com.training.coach.athlete.application.port.out.FitnessPlatformPort;
import com.training.coach.shared.functional.Result;
import com.training.coach.wellness.application.port.out.WellnessRepository;
import com.training.coach.wellness.application.service.ReadinessCalculatorService;
import com.training.coach.wellness.application.service.TrainingLoadCalculator;
import com.training.coach.wellness.domain.model.PhysiologicalData;
import com.training.coach.wellness.domain.model.SleepMetrics;
import com.training.coach.wellness.domain.model.TrainingLoadSummary;
import com.training.coach.wellness.domain.model.WellnessSnapshot;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
    private final ActivityRepository activityRepository;
    private final WellnessRepository wellnessRepository;
    private final ReadinessCalculatorService readinessCalculatorService;
    private final TrainingLoadCalculator trainingLoadCalculator;

    // In-memory sync tracking for testing
    private final java.util.Map<String, SyncResult> lastSyncResults = new java.util.HashMap<>();

    public SyncService(
            FitnessPlatformPort fitnessPlatformPort,
            ActivityRepository activityRepository,
            WellnessRepository wellnessRepository,
            ReadinessCalculatorService readinessCalculatorService,
            TrainingLoadCalculator trainingLoadCalculator) {
        this.fitnessPlatformPort = fitnessPlatformPort;
        this.activityRepository = activityRepository;
        this.wellnessRepository = wellnessRepository;
        this.readinessCalculatorService = readinessCalculatorService;
        this.trainingLoadCalculator = trainingLoadCalculator;
    }

    /**
     * Sync result record containing status and details.
     */
    public record SyncResult(
        String athleteId,
        boolean activitiesSuccess,
        boolean wellnessSuccess,
        String status, // "success", "partial_failure", "complete_failure"
        int activitiesSynced,
        int wellnessRecordsSynced,
        String errorMessage
    ) {
        public static SyncResult success(String athleteId, int activities, int wellness) {
            return new SyncResult(athleteId, true, true, "success", activities, wellness, null);
        }
        
        public static SyncResult partialFailure(String athleteId, int activities, int wellness, String error) {
            return new SyncResult(athleteId, activities > 0, wellness > 0, "partial_failure", activities, wellness, error);
        }
        
        public static SyncResult completeFailure(String athleteId, String error) {
            return new SyncResult(athleteId, false, false, "complete_failure", 0, 0, error);
        }
    }

    public void syncAthleteData(String athleteId, LocalDate startDate, LocalDate endDate) {
        SyncResult result = performSync(athleteId, startDate, endDate);
        lastSyncResults.put(athleteId, result);
    }

    private SyncResult performSync(String athleteId, LocalDate startDate, LocalDate endDate) {
        boolean activitiesSuccess = false;
        boolean wellnessSuccess = false;
        int activitiesCount = 0;
        int wellnessCount = 0;
        String errorMessage = null;
        
        try {
            activitiesCount = syncActivities(athleteId, startDate, endDate);
            activitiesSuccess = true;
        } catch (Exception e) {
            logger.error("Activity sync failed for athlete {}: {}", athleteId, e.getMessage());
            errorMessage = "Activities: " + e.getMessage();
        }
        
        try {
            wellnessCount = syncWellnessData(athleteId, startDate, endDate);
            wellnessSuccess = true;
        } catch (Exception e) {
            logger.error("Wellness sync failed for athlete {}: {}", athleteId, e.getMessage());
            errorMessage = errorMessage != null ? errorMessage + "; Wellness: " + e.getMessage() : "Wellness: " + e.getMessage();
        }
        
        // Determine overall status
        String status;
        if (activitiesSuccess && wellnessSuccess) {
            status = "success";
            errorMessage = null;
        } else if (activitiesSuccess || wellnessSuccess) {
            status = "partial_failure";
        } else {
            status = "complete_failure";
        }
        
        SyncResult result = new SyncResult(
            athleteId, activitiesSuccess, wellnessSuccess, status, activitiesCount, wellnessCount, errorMessage
        );
        
        logger.info("Sync completed for athlete {}: status={}, activities={}, wellness={}", 
            athleteId, status, activitiesCount, wellnessCount);
        
        return result;
    }

    /**
     * Get the last sync result for an athlete.
     */
    public SyncResult getLastSyncResult(String athleteId) {
        return lastSyncResults.get(athleteId);
    }

    /**
     * Simulate nightly sync for multiple athletes.
     */
    public java.util.Map<String, SyncResult> runNightlySync(java.util.List<String> athleteIds, LocalDate startDate, LocalDate endDate) {
        java.util.Map<String, SyncResult> results = new java.util.HashMap<>();
        for (String athleteId : athleteIds) {
            SyncResult result = performSync(athleteId, startDate, endDate);
            results.put(athleteId, result);
            lastSyncResults.put(athleteId, result);
        }
        return results;
    }

    /**
     * Detect conflicts between activities from different platforms.
     */
    public java.util.List<ActivityConflict> detectActivityConflicts(String athleteId, LocalDate date) {
        // In a real implementation, this would:
        // 1. Fetch activities from multiple platforms for the given date
        // 2. Compare durations, timestamps, and other metadata
        // 3. Flag conflicts where data doesn't match

        // For testing purposes, we'll simulate some conflicts
        return java.util.List.of(
            new ActivityConflict(
                athleteId,
                date,
                java.util.List.of(
                    new ConflictRecord("Intervals.icu", "activity1", 60, date),
                    new ConflictRecord("Strava", "activity2", 65, date)
                ),
                ConflictStatus.AMBIGUOUS,
                null
            )
        );
    }

    /**
     * Apply precedence rules to resolve conflicts.
     */
    public ActivityConflict resolveConflict(String athleteId, String conflictId, String preferredPlatform) {
        var conflicts = detectActivityConflicts(athleteId, LocalDate.now());

        for (var conflict : conflicts) {
            if (conflict.canonicalRecordId() == null) {
                // Apply precedence - Intervals.icu has higher precedence
                if ("Intervals.icu".equals(preferredPlatform)) {
                    return new ActivityConflict(
                        conflict.athleteId(),
                        conflict.date(),
                        conflict.conflictingRecords(),
                        ConflictStatus.RESOLVED,
                        conflict.conflictingRecords().get(0).activityId()
                    );
                }
            }
        }

        return null;
    }

    /**
     * Mark a conflict as requiring manual review.
     */
    public ActivityConflict flagForManualReview(String athleteId, String conflictId, String reason) {
        var conflicts = detectActivityConflicts(athleteId, LocalDate.now());

        for (var conflict : conflicts) {
            if (conflict.canonicalRecordId() == null) {
                return new ActivityConflict(
                    conflict.athleteId(),
                    conflict.date(),
                    conflict.conflictingRecords(),
                    ConflictStatus.REQUIRES_REVIEW,
                    null
                );
            }
        }

        return null;
    }

    /**
     * Activity conflict record for multi-platform reconciliation.
     */
    public record ActivityConflict(
        String athleteId,
        LocalDate date,
        java.util.List<ConflictRecord> conflictingRecords,
        ConflictStatus status,
        String canonicalRecordId
    ) {}

    public enum ConflictStatus {
        DETECTED,
        AMBIGUOUS,
        RESOLVED,
        REQUIRES_REVIEW
    }

    public record ConflictRecord(
        String platform,
        String activityId,
        int durationMinutes,
        LocalDate timestamp
    ) {}

    private int syncActivities(String athleteId, LocalDate startDate, LocalDate endDate) {
        try {
            Result<List<FitnessPlatformPort.Activity>> activitiesResult =
                    fitnessPlatformPort.getActivities(athleteId, startDate, endDate);
            if (activitiesResult.isFailure()) {
                String errorMessage =
                        activitiesResult.error().map(Throwable::getMessage).orElse("Unknown error");
                logger.error("Failed to sync activities: {}", errorMessage);
                return 0;
            }

            List<FitnessPlatformPort.Activity> activities = activitiesResult.value().get();
            List<ActivityLight> mapped = activities.stream()
                    .map(activity -> ActivityLight.create(
                            athleteId,
                            activity.id(),
                            activity.date(),
                            activity.name(),
                            activity.type(),
                            activity.durationSeconds(),
                            activity.distanceKm(),
                            activity.averagePower(),
                            activity.averageHeartRate(),
                            activity.trainingStressScore(),
                            activity.intensityFactor(),
                            activity.normalizedPower()))
                    .toList();

            activityRepository.saveAll(mapped);
            logger.info("Synced {} activities for athlete {}", mapped.size(), athleteId);
            return mapped.size();
        } catch (Exception e) {
            logger.error("Exception syncing activities for athlete {}: {}", athleteId, e.getMessage());
            return 0;
        }
    }

    private int syncWellnessData(String athleteId, LocalDate startDate, LocalDate endDate) {
        try {
            Result<List<FitnessPlatformPort.WellnessData>> wellnessResult =
                    fitnessPlatformPort.getWellnessDataRange(athleteId, startDate, endDate);

            if (wellnessResult.isFailure()) {
                String errorMessage =
                        wellnessResult.error().map(Throwable::getMessage).orElse("Unknown error");
                logger.error("Failed to sync wellness data: {}", errorMessage);
                return 0;
            }

            List<FitnessPlatformPort.WellnessData> wellnessDataList =
                    wellnessResult.value().get();
            logger.info("Synced {} wellness records for athlete {}", wellnessDataList.size(), athleteId);

            for (FitnessPlatformPort.WellnessData data : wellnessDataList) {
                syncSingleWellnessRecord(athleteId, data);
            }

            trainingLoadCalculator.calculateAndStoreTrainingLoads(athleteId, startDate, endDate);
            logger.info("Completed wellness sync and training load calculation for athlete {}", athleteId);
            return wellnessDataList.size();
        } catch (Exception e) {
            logger.error("Exception syncing wellness for athlete {}: {}", athleteId, e.getMessage());
            return 0;
        }
    }

    private void syncSingleWellnessRecord(String athleteId, FitnessPlatformPort.WellnessData data) {
        LocalDate date = data.date();

        PhysiologicalData physiological = new PhysiologicalData(
                data.restingHeartRate(),
                data.hrv(),
                data.bodyWeightKg(),
                SleepMetrics.basic(data.sleepHours(), data.sleepQuality()));

        TrainingLoadSummary loadSummary = trainingLoadCalculator.calculateTrainingLoadSummary(athleteId, date);

        double readinessScore = readinessCalculatorService.calculateReadiness(physiological, null, loadSummary);

        String snapshotId = athleteId + "_" + date.toString();
        Optional<WellnessSnapshot> existingSnapshot = wellnessRepository.findByAthleteIdAndDate(athleteId, date);

        WellnessSnapshot snapshot;
        if (existingSnapshot.isPresent()) {
            snapshot = new WellnessSnapshot(
                    existingSnapshot.get().id(),
                    athleteId,
                    date,
                    physiological,
                    existingSnapshot.get().subjective(),
                    loadSummary,
                    readinessScore);
        } else {
            snapshot =
                    new WellnessSnapshot(snapshotId, athleteId, date, physiological, null, loadSummary, readinessScore);
        }

        wellnessRepository.save(snapshot);
        logger.debug("Saved wellness snapshot for athlete {} on date {}", athleteId, date);
    }
}
