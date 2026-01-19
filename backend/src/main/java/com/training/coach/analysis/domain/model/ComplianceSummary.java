package com.training.coach.analysis.domain.model;

import java.util.List;

public record ComplianceSummary(
        double completionPercent,
        double keySessionCompletionPercent,
        double zoneDistributionAdherencePercent,
        List<String> flags,
        double unplannedLoadMinutes) {
}
