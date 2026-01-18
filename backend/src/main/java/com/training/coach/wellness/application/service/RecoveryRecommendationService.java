package com.training.coach.wellness.application.service;

import com.training.coach.integration.application.service.AIService;
import com.training.coach.wellness.application.port.out.WellnessRepository;
import com.training.coach.wellness.domain.model.RecoveryRecommendations;
import com.training.coach.wellness.domain.model.WellnessSnapshot;
import com.training.coach.wellness.domain.model.WellnessTrends;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RecoveryRecommendationService {

    private static final double LOW_READINESS_THRESHOLD = 30.0;
    private static final double MODERATE_READINESS_THRESHOLD = 50.0;
    private static final double HIGH_TSB_NEGATIVE = -20.0;
    private static final double LOW_SLEEP_HOURS = 6.0;
    private static final int LOW_SLEEP_DAYS = 3;

    private final WellnessRepository wellnessRepository;
    private final TrendCalculationService trendService;
    private final AIService aiService;

    public RecoveryRecommendationService(
            WellnessRepository wellnessRepository, TrendCalculationService trendService, AIService aiService) {
        this.wellnessRepository = wellnessRepository;
        this.trendService = trendService;
        this.aiService = aiService;
    }

    public RecoveryRecommendations generateRecommendations(String athleteId, LocalDate startDate, LocalDate endDate) {
        List<WellnessSnapshot> snapshots =
                wellnessRepository.findByAthleteIdAndDateRange(athleteId, startDate, endDate);

        if (snapshots.isEmpty()) {
            return RecoveryRecommendations.empty(athleteId);
        }

        WellnessSnapshot latest = snapshots.get(snapshots.size() - 1);
        WellnessTrends trends = trendService.calculateTrends(snapshots);

        List<RecoveryRecommendations.HardRule> hardRules = new ArrayList<>();
        List<String> safeAdjustments = new ArrayList<>();
        double readinessScore = latest.readinessScore();

        if (readinessScore < LOW_READINESS_THRESHOLD) {
            hardRules.add(new RecoveryRecommendations.HardRule(
                    "CRITICAL_LOW_READINESS",
                    "Readiness score below 30",
                    "REST DAY REQUIRED - Athlete is severely fatigued. No training recommended.",
                    true));
        } else if (readinessScore < MODERATE_READINESS_THRESHOLD) {
            hardRules.add(new RecoveryRecommendations.HardRule(
                    "LOW_READINESS",
                    "Readiness score between 30-50",
                    "Consider reducing training intensity. Focus on recovery workouts.",
                    false));
        }

        if (trends.hrvTrend() == WellnessTrends.TrendDirection.DECLINING
                || trends.hrvTrend() == WellnessTrends.TrendDirection.SLIGHT_DECLINE) {
            hardRules.add(new RecoveryRecommendations.HardRule(
                    "DECLINING_HRV",
                    "HRV trending downward",
                    "Monitor for signs of overreaching. Consider reducing training load.",
                    false));
        }

        int lowSleepDays = 0;
        for (WellnessSnapshot s : snapshots) {
            if (s.physiological() != null
                    && s.physiological().sleep() != null
                    && s.physiological().sleep().totalSleepHours().value() < LOW_SLEEP_HOURS) {
                lowSleepDays++;
            }
        }
        if (lowSleepDays >= LOW_SLEEP_DAYS) {
            hardRules.add(new RecoveryRecommendations.HardRule(
                    "CHRONIC_SLEEP_DEBT",
                    "Less than 6 hours sleep for " + LOW_SLEEP_DAYS + "+ days",
                    "Prioritize sleep hygiene. Reduce training to allow recovery.",
                    false));
        }

        if (latest.loadSummary() != null && latest.loadSummary().tsb() < HIGH_TSB_NEGATIVE) {
            hardRules.add(new RecoveryRecommendations.HardRule(
                    "HIGH_FATIGUE_TSB",
                    "Training Stress Balance below -20",
                    "High accumulated fatigue. Consider easy day or rest day.",
                    false));
        }

        if (readinessScore >= MODERATE_READINESS_THRESHOLD) {
            safeAdjustments.add("Maintain current training plan");
            safeAdjustments.add("Include 1-2 recovery-focused workouts this week");
        } else if (readinessScore >= LOW_READINESS_THRESHOLD) {
            safeAdjustments.add("Reduce training volume by 20%");
            safeAdjustments.add("Replace high-intensity intervals with steady endurance");
            safeAdjustments.add("Add extra recovery day");
        } else {
            safeAdjustments.add("Rest day recommended");
            safeAdjustments.add("Light activity only (walking, stretching)");
            safeAdjustments.add("Focus on sleep and nutrition");
        }

        if (trends.readinessTrend() == WellnessTrends.TrendDirection.IMPROVING
                || trends.readinessTrend() == WellnessTrends.TrendDirection.SLIGHT_IMPROVEMENT) {
            safeAdjustments.add("Readiness improving - gradual load increase OK");
        }

        String aiRecommendations = "";
        try {
            String prompt = buildAiPrompt(athleteId, latest, trends, hardRules);
            aiRecommendations = aiService.getSuggestion(prompt);
        } catch (Exception e) {
            aiRecommendations = "AI recommendations temporarily unavailable.";
        }

        return new RecoveryRecommendations(
                athleteId, hardRules, aiRecommendations, safeAdjustments, readinessScore, 0.0);
    }

    private String buildAiPrompt(
            String athleteId,
            WellnessSnapshot latest,
            WellnessTrends trends,
            List<RecoveryRecommendations.HardRule> rules) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate recovery recommendations for athlete ")
                .append(athleteId)
                .append(". ");
        prompt.append("Current readiness: ")
                .append(String.format("%.1f", latest.readinessScore()))
                .append("/100. ");
        prompt.append("HRV trend: ").append(trends.hrvTrend()).append(". ");
        prompt.append("Sleep trend: ").append(trends.sleepHoursTrend()).append(". ");
        prompt.append("Average sleep hours: ")
                .append(String.format("%.1f", trends.averageSleepHours()))
                .append(". ");

        if (!rules.isEmpty()) {
            prompt.append("Hard rules triggered: ");
            for (var rule : rules) {
                prompt.append(rule.ruleName()).append(", ");
            }
            prompt.setLength(prompt.length() - 2);
            prompt.append(". ");
        }

        prompt.append("Provide specific, actionable recovery advice. Keep response under 200 words.");
        return prompt.toString();
    }
}
