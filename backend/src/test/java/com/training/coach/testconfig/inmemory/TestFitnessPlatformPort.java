package com.training.coach.testconfig.inmemory;

import com.training.coach.athlete.application.port.out.FitnessPlatformPort;
import com.training.coach.shared.functional.Result;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * In-memory FitnessPlatformPort for fast tests.
 */
public class TestFitnessPlatformPort implements FitnessPlatformPort {
    private List<Activity> activities;
    private List<WellnessData> wellnessData;

    public void setActivities(List<Activity> newActivities) {
        ensureInitialized();
        activities.clear();
        activities.addAll(newActivities);
    }

    public void setWellnessData(List<WellnessData> newWellnessData) {
        ensureInitialized();
        wellnessData.clear();
        wellnessData.addAll(newWellnessData);
    }

    public void clear() {
        ensureInitialized();
        activities.clear();
        wellnessData.clear();
    }

    private void ensureInitialized() {
        if (activities == null) {
            activities = new ArrayList<>();
        }
        if (wellnessData == null) {
            wellnessData = new ArrayList<>();
        }
    }

    @Override
    public Result<List<Activity>> getActivities(String athleteId, LocalDate startDate, LocalDate endDate) {
        ensureInitialized();
        List<Activity> filtered = activities.stream()
                .filter(activity -> !activity.date().isBefore(startDate) && !activity.date().isAfter(endDate))
                .toList();
        return Result.success(filtered);
    }

    @Override
    public Result<WellnessData> getWellnessData(String athleteId, LocalDate date) {
        ensureInitialized();
        WellnessData found = wellnessData.stream()
                .filter(data -> data.date().equals(date))
                .findFirst()
                .orElse(null);
        if (found == null) {
            return Result.failure(new IllegalStateException("No wellness data for " + date));
        }
        return Result.success(found);
    }

    @Override
    public Result<List<WellnessData>> getWellnessDataRange(String athleteId, LocalDate startDate, LocalDate endDate) {
        ensureInitialized();
        List<WellnessData> filtered = wellnessData.stream()
                .filter(data -> !data.date().isBefore(startDate) && !data.date().isAfter(endDate))
                .toList();
        return Result.success(filtered);
    }
}
