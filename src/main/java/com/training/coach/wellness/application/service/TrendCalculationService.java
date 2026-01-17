package com.training.coach.wellness.application.service;

import com.training.coach.wellness.domain.model.WellnessSnapshot;
import com.training.coach.wellness.domain.model.WellnessTrends;
import com.training.coach.wellness.domain.model.WellnessTrends.TrendDirection;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TrendCalculationService {

    private static final int MIN_DATA_POINTS = 3;
    private static final double SIGNIFICANT_CHANGE_THRESHOLD = 0.05;

    public WellnessTrends calculateTrends(List<WellnessSnapshot> snapshots) {
        if (snapshots == null || snapshots.size() < MIN_DATA_POINTS) {
            return WellnessTrends.empty();
        }

        List<WellnessSnapshot> sortedSnapshots = snapshots.stream()
                .sorted((a, b) -> a.date().compareTo(b.date()))
                .toList();

        TrendDirection rhrTrend = calculateTrend(sortedSnapshots.stream()
                .filter(s -> s.physiological() != null && s.physiological().restingHeartRate() != null)
                .mapToDouble(s -> s.physiological().restingHeartRate().value())
                .boxed()
                .toList());

        TrendDirection hrvTrend = calculateTrend(sortedSnapshots.stream()
                .filter(s -> s.physiological() != null && s.physiological().hrv() != null)
                .mapToDouble(s -> s.physiological().hrv().value())
                .boxed()
                .toList());

        TrendDirection bodyWeightTrend = calculateTrend(sortedSnapshots.stream()
                .filter(s -> s.physiological() != null && s.physiological().bodyWeightKg() != null)
                .mapToDouble(s -> s.physiological().bodyWeightKg().value())
                .boxed()
                .toList());

        TrendDirection sleepTrend = calculateTrend(sortedSnapshots.stream()
                .filter(s -> s.physiological() != null && s.physiological().sleep() != null)
                .mapToDouble(s -> s.physiological().sleep().totalSleepHours().value())
                .boxed()
                .toList());

        TrendDirection readinessTrend = calculateTrend(sortedSnapshots.stream()
                .mapToDouble(WellnessSnapshot::readinessScore)
                .boxed()
                .toList());

        TrendDirection tssTrend = calculateTrend(sortedSnapshots.stream()
                .filter(s -> s.loadSummary() != null)
                .mapToDouble(s -> s.loadSummary().tss())
                .boxed()
                .toList());

        double averageReadiness = sortedSnapshots.stream()
                .mapToDouble(WellnessSnapshot::readinessScore)
                .average()
                .orElse(0.0);

        double readinessVariance = calculateVariance(sortedSnapshots.stream()
                .mapToDouble(WellnessSnapshot::readinessScore)
                .boxed()
                .toList());

        double averageHrv = sortedSnapshots.stream()
                .filter(s -> s.physiological() != null && s.physiological().hrv() != null)
                .mapToDouble(s -> s.physiological().hrv().value())
                .average()
                .orElse(0.0);

        double averageRhr = sortedSnapshots.stream()
                .filter(s -> s.physiological() != null && s.physiological().restingHeartRate() != null)
                .mapToDouble(s -> s.physiological().restingHeartRate().value())
                .average()
                .orElse(0.0);

        double averageSleepHours = sortedSnapshots.stream()
                .filter(s -> s.physiological() != null && s.physiological().sleep() != null)
                .mapToDouble(s -> s.physiological().sleep().totalSleepHours().value())
                .average()
                .orElse(0.0);

        return new WellnessTrends(
                rhrTrend,
                hrvTrend,
                bodyWeightTrend,
                sleepTrend,
                readinessTrend,
                tssTrend,
                averageReadiness,
                readinessVariance,
                averageHrv,
                averageRhr,
                averageSleepHours);
    }

    private TrendDirection calculateTrend(List<Double> values) {
        if (values.size() < MIN_DATA_POINTS) {
            return TrendDirection.INSUFFICIENT_DATA;
        }

        double firstHalfAverage = calculateAverage(values.subList(0, values.size() / 2));
        double secondHalfAverage = calculateAverage(values.subList(values.size() / 2, values.size()));

        double change = (secondHalfAverage - firstHalfAverage) / firstHalfAverage;

        if (change > SIGNIFICANT_CHANGE_THRESHOLD) {
            return TrendDirection.IMPROVING;
        } else if (change > 0) {
            return TrendDirection.SLIGHT_IMPROVEMENT;
        } else if (change < -SIGNIFICANT_CHANGE_THRESHOLD) {
            return TrendDirection.DECLINING;
        } else if (change < 0) {
            return TrendDirection.SLIGHT_DECLINE;
        } else {
            return TrendDirection.STABLE;
        }
    }

    private double calculateAverage(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private double calculateVariance(List<Double> values) {
        if (values.size() < 2) return 0.0;
        double mean = calculateAverage(values);
        return values.stream().mapToDouble(v -> Math.pow(v - mean, 2)).average().orElse(0.0);
    }
}
