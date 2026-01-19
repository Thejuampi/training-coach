package com.training.coach.analysis.application.service;

import com.training.coach.activity.domain.model.ActivityLight;
import com.training.coach.analysis.domain.model.ComplianceSummary;
import com.training.coach.analysis.domain.model.ProgressSummary;
import com.training.coach.athlete.domain.model.Workout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ComplianceProgressService {

    public ComplianceSummary summarizeWeeklyCompliance(
            List<Workout> plannedWorkouts,
            List<ActivityLight> completedActivities,
            Map<String, Double> zoneMinutes,
            Map<String, String> activityClassifications,
            LocalDate rangeStart,
            LocalDate rangeEnd) {
        List<Workout> plannedInRange = plannedWorkouts.stream()
                .filter(workout -> !workout.date().isBefore(rangeStart) && !workout.date().isAfter(rangeEnd))
                .collect(Collectors.toList());

        Set<LocalDate> completedDates = completedActivities.stream().map(ActivityLight::date).collect(Collectors.toSet());
        int plannedCount = plannedInRange.size();
        int completedCount = completedActivities.size();
        double completionPercent = plannedCount == 0 ? 100.0 : Math.min(100.0, ((double) completedCount / plannedCount) * 100.0);

        List<Workout> keySessions = plannedInRange.stream()
                .filter(workout -> workout.type() == Workout.WorkoutType.THRESHOLD || workout.type() == Workout.WorkoutType.INTERVALS)
                .collect(Collectors.toList());
        int keySessionCount = keySessions.size();
        long completedKeySessions = keySessions.stream().filter(workout -> completedDates.contains(workout.date())).count();
        double keySessionCompletionPercent = keySessionCount == 0 ? 100.0 : Math.min(100.0, ((double) completedKeySessions / keySessionCount) * 100.0);

        double totalZoneMinutes = zoneMinutes.values().stream().mapToDouble(Double::doubleValue).sum();
        double adherence = totalZoneMinutes == 0 ? 100.0 : Math.max(0.0, 100.0 - computeZoneDeviationPercent(plannedInRange, zoneMinutes, totalZoneMinutes));

        List<String> flags = new ArrayList<>();
        if (totalZoneMinutes > 0 && zoneMinutes.getOrDefault("Z2", 0.0) / totalZoneMinutes > 0.7) {
            flags.add("Z2_CREEP");
        }

        double unplannedLoadMinutes = calculateUnplannedLoadMinutes(plannedInRange, completedActivities, activityClassifications);

        return new ComplianceSummary(completionPercent, keySessionCompletionPercent, adherence, flags, unplannedLoadMinutes);
    }

    public ProgressSummary summarizeProgress(List<Double> weeklyVolumes, List<Double> trainingLoads) {
        List<Double> volumes = List.copyOf(weeklyVolumes);
        List<Double> loads = List.copyOf(trainingLoads);
        int streak = 0;
        for (int index = volumes.size() - 1; index >= 0; index--) {
            if (index < 0) {
                break;
            }
            if (volumes.get(index) > 0) {
                streak++;
            } else {
                break;
            }
        }
        return new ProgressSummary(volumes, loads, streak);
    }

    private double computeZoneDeviationPercent(List<Workout> plannedWorkouts, Map<String, Double> actualZones, double totalActualMinutes) {
        if (totalActualMinutes == 0 || plannedWorkouts.isEmpty()) {
            return 0.0;
        }
        Map<String, Double> plannedPercentages = aggregatePlannedZonePercentages(plannedWorkouts);
        double actualZ1Percent = actualZones.getOrDefault("Z1", 0.0) / totalActualMinutes * 100.0;
        double actualZ2Percent = actualZones.getOrDefault("Z2", 0.0) / totalActualMinutes * 100.0;
        double actualZ3Percent = actualZones.getOrDefault("Z3", 0.0) / totalActualMinutes * 100.0;
        double targetZ1 = plannedPercentages.getOrDefault("Z1", 0.0);
        double targetZ2 = plannedPercentages.getOrDefault("Z2", 0.0);
        double targetZ3 = plannedPercentages.getOrDefault("Z3", 0.0);
        return Math.abs(targetZ1 - actualZ1Percent) + Math.abs(targetZ2 - actualZ2Percent) + Math.abs(targetZ3 - actualZ3Percent);
    }

    private Map<String, Double> aggregatePlannedZonePercentages(List<Workout> plannedWorkouts) {
        Map<String, Double> totals = new HashMap<>();
        double totalDuration = 0.0;
        for (Workout workout : plannedWorkouts) {
            double minutes = workout.durationMinutes().value();
            totalDuration += minutes;
            totals.merge("Z1", workout.intensityProfile().zone1Percent().value() * minutes, Double::sum);
            totals.merge("Z2", workout.intensityProfile().zone2Percent().value() * minutes, Double::sum);
            totals.merge("Z3", workout.intensityProfile().zone3Percent().value() * minutes, Double::sum);
        }
        Map<String, Double> percentages = new HashMap<>();
        if (totalDuration == 0) {
            return percentages;
        }
        percentages.put("Z1", totals.getOrDefault("Z1", 0.0) / totalDuration);
        percentages.put("Z2", totals.getOrDefault("Z2", 0.0) / totalDuration);
        percentages.put("Z3", totals.getOrDefault("Z3", 0.0) / totalDuration);
        return percentages;
    }

    private double calculateUnplannedLoadMinutes(
            List<Workout> plannedWorkouts,
            List<ActivityLight> activities,
            Map<String, String> classifications) {
        Set<LocalDate> plannedDates = plannedWorkouts.stream().map(Workout::date).collect(Collectors.toSet());
        double unplannedMinutes = 0.0;
        for (ActivityLight activity : activities) {
            double durationMinutes = activity.durationSeconds().value() / 60.0;
            boolean matchesPlan = plannedDates.contains(activity.date());
            String classification = classifications.get(activity.externalActivityId());
            if (!matchesPlan || "ad_hoc".equalsIgnoreCase(classification)) {
                unplannedMinutes += durationMinutes;
            }
        }
        return unplannedMinutes;
    }
}
