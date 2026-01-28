package com.training.coach.reporting.domain.model;

import java.util.List;

/**
 * Organization-level summary report for admin use.
 * Aggregates data across all athletes for a given date range.
 */
public record OrganizationReport(
        int totalAthletes,
        int activeAthletes,
        double averageReadiness,
        double averageCompliance,
        List<AthleteSummary> athleteSummaries
) {
    public record AthleteSummary(
            String athleteId,
            String athleteName,
            double readinessScore,
            double compliancePercent,
            int recentActivitiesCount
    ) {}
}
