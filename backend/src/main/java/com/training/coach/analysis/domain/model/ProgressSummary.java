package com.training.coach.analysis.domain.model;

import java.util.List;

public record ProgressSummary(
        List<Double> weeklyVolumeTrend,
        List<Double> trainingLoadTrend,
        int completionStreak) {
}
