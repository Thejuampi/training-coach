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

    public void syncAthleteData(String athleteId, LocalDate startDate, LocalDate endDate) {
        syncActivities(athleteId, startDate, endDate);
        syncWellnessData(athleteId, startDate, endDate);
    }

    private void syncActivities(String athleteId, LocalDate startDate, LocalDate endDate) {
        Result<List<FitnessPlatformPort.Activity>> activitiesResult =
                fitnessPlatformPort.getActivities(athleteId, startDate, endDate);
        if (activitiesResult.isFailure()) {
            String errorMessage =
                    activitiesResult.error().map(Throwable::getMessage).orElse("Unknown error");
            logger.error("Failed to sync activities: {}", errorMessage);
            return;
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
    }

    private void syncWellnessData(String athleteId, LocalDate startDate, LocalDate endDate) {
        Result<List<FitnessPlatformPort.WellnessData>> wellnessResult =
                fitnessPlatformPort.getWellnessDataRange(athleteId, startDate, endDate);

        if (wellnessResult.isFailure()) {
            String errorMessage =
                    wellnessResult.error().map(Throwable::getMessage).orElse("Unknown error");
            logger.error("Failed to sync wellness data: {}", errorMessage);
            return;
        }

        List<FitnessPlatformPort.WellnessData> wellnessDataList =
                wellnessResult.value().get();
        logger.info("Synced {} wellness records for athlete {}", wellnessDataList.size(), athleteId);

        for (FitnessPlatformPort.WellnessData data : wellnessDataList) {
            syncSingleWellnessRecord(athleteId, data);
        }

        trainingLoadCalculator.calculateAndStoreTrainingLoads(athleteId, startDate, endDate);
        logger.info("Completed wellness sync and training load calculation for athlete {}", athleteId);
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
