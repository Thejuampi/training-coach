package com.training.coach.wellness.domain.model;

import java.util.ArrayList;
import java.util.List;

public record WellnessInsights(
        List<String> flags,
        List<String> recommendations,
        int complianceRate,
        double trainingVolumeHours,
        List<String> achievements) {
    public WellnessInsights {
        if (flags == null) flags = new ArrayList<>();
        if (recommendations == null) recommendations = new ArrayList<>();
        if (achievements == null) achievements = new ArrayList<>();
        if (complianceRate < 0 || complianceRate > 100) {
            throw new IllegalArgumentException("Compliance rate must be between 0 and 100");
        }
        if (trainingVolumeHours < 0) {
            throw new IllegalArgumentException("Training volume hours must be non-negative");
        }
    }

    public static WellnessInsights empty() {
        return new WellnessInsights(new ArrayList<>(), new ArrayList<>(), 0, 0.0, new ArrayList<>());
    }

    public WellnessInsights withFlag(String flag) {
        List<String> newFlags = new ArrayList<>(this.flags);
        newFlags.add(flag);
        return new WellnessInsights(newFlags, recommendations, complianceRate, trainingVolumeHours, achievements);
    }

    public WellnessInsights withRecommendation(String recommendation) {
        List<String> newRecommendations = new ArrayList<>(this.recommendations);
        newRecommendations.add(recommendation);
        return new WellnessInsights(flags, newRecommendations, complianceRate, trainingVolumeHours, achievements);
    }

    public WellnessInsights withAchievement(String achievement) {
        List<String> newAchievements = new ArrayList<>(this.achievements);
        newAchievements.add(achievement);
        return new WellnessInsights(flags, recommendations, complianceRate, trainingVolumeHours, newAchievements);
    }
}
