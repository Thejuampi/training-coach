package com.training.coach.wellness.application.service;

import com.training.coach.wellness.domain.model.WellnessInsights;
import com.training.coach.wellness.domain.model.WellnessSnapshot;
import com.training.coach.wellness.domain.model.WellnessTrends;
import com.training.coach.wellness.domain.model.WellnessTrends.TrendDirection;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WellnessInsightsService {

    private static final double LOW_READINESS_THRESHOLD = 50.0;
    private static final double DECLINING_HRV_THRESHOLD = 0.10;
    private static final double LOW_SLEEP_HOURS = 6.0;
    private static final double HIGH_TSB_NEGATIVE = -20.0;

    public WellnessInsights generateInsights(
            List<WellnessSnapshot> snapshots, WellnessTrends trends, int complianceRate, double trainingVolumeHours) {
        List<String> flags = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        List<String> achievements = new ArrayList<>();

        if (trends.averageReadinessScore() < LOW_READINESS_THRESHOLD) {
            flags.add("Average readiness score below 50 - athlete may be fatigued");
            recommendations.add("Consider reducing training load for 1-2 days");
            recommendations.add("Focus on recovery activities and sleep hygiene");
        }

        if (trends.hrvTrend() == TrendDirection.DECLINING || trends.hrvTrend() == TrendDirection.SLIGHT_DECLINE) {
            flags.add("HRV trending downward - possible overreaching");
            recommendations.add("Monitor closely for signs of overtraining");
        }

        if (trends.sleepHoursTrend() == TrendDirection.DECLINING
                || trends.sleepHoursTrend() == TrendDirection.SLIGHT_DECLINE) {
            flags.add("Sleep duration decreasing");
            recommendations.add("Prioritize sleep hygiene and recovery");
        }

        WellnessSnapshot latestSnapshot =
                snapshots.stream().max((a, b) -> a.date().compareTo(b.date())).orElse(null);

        if (latestSnapshot != null && latestSnapshot.loadSummary() != null) {
            double tsb = latestSnapshot.loadSummary().tsb();
            if (tsb < HIGH_TSB_NEGATIVE) {
                flags.add("Training Stress Balance heavily negative - high fatigue");
                recommendations.add("Consider an easy day or rest day");
            }
        }

        if (complianceRate >= 90) {
            achievements.add("Excellent training compliance (" + complianceRate + "%)");
        } else if (complianceRate >= 80) {
            achievements.add("Good training compliance (" + complianceRate + "%)");
        }

        if (trends.readinessTrend() == TrendDirection.IMPROVING
                || trends.readinessTrend() == TrendDirection.SLIGHT_IMPROVEMENT) {
            achievements.add("Readiness improving over the period");
        }

        if (trends.hrvTrend() == TrendDirection.IMPROVING || trends.hrvTrend() == TrendDirection.SLIGHT_IMPROVEMENT) {
            achievements.add("HRV improving - athlete adapting well to training");
        }

        if (complianceRate < 70) {
            recommendations.add("Training compliance below 70% - review schedule");
        }

        if (trainingVolumeHours > 15) {
            recommendations.add("High training volume - ensure adequate recovery");
        }

        return new WellnessInsights(flags, recommendations, complianceRate, trainingVolumeHours, achievements);
    }
}
