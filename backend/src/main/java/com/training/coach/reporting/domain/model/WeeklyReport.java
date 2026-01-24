package com.training.coach.reporting.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Domain model for a comprehensive weekly training report.
 * Includes readiness trends, compliance summary, and key notes.
 */
public record WeeklyReport(
        String id,
        String athleteId,
        String athleteName,
        LocalDate weekStart,
        LocalDate weekEnd,
        ReadinessTrend readinessTrend,
        ComplianceSummary compliance,
        List<String> keyNotes,
        List<String> completedActivities,
        ReportMetadata metadata
) {
    public WeeklyReport {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Report ID cannot be null or blank");
        }
        if (athleteId == null || athleteId.isBlank()) {
            throw new IllegalArgumentException("Athlete ID cannot be null or blank");
        }
        if (athleteName == null || athleteName.isBlank()) {
            throw new IllegalArgumentException("Athlete name cannot be null or blank");
        }
        if (weekStart == null || weekEnd == null) {
            throw new IllegalArgumentException("Week dates cannot be null");
        }
        if (weekStart.isAfter(weekEnd)) {
            throw new IllegalArgumentException("Week start must be before or equal to week end");
        }
        if (readinessTrend == null) readinessTrend = ReadinessTrend.empty();
        if (compliance == null) compliance = ComplianceSummary.empty();
        if (keyNotes == null) keyNotes = List.of();
        if (completedActivities == null) completedActivities = List.of();
        if (metadata == null) metadata = ReportMetadata.now();
    }

    public int numberOfDays() {
        return (int) (weekEnd.toEpochDay() - weekStart.toEpochDay()) + 1;
    }

    public double averageReadiness() {
        return readinessTrend.dailyScores().values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    public static WeeklyReport create(
            String athleteId,
            String athleteName,
            LocalDate weekStart,
            LocalDate weekEnd,
            ReadinessTrend readinessTrend,
            ComplianceSummary compliance,
            List<String> keyNotes,
            List<String> completedActivities
    ) {
        return new WeeklyReport(
                UUID.randomUUID().toString(),
                athleteId,
                athleteName,
                weekStart,
                weekEnd,
                readinessTrend,
                compliance,
                keyNotes,
                completedActivities,
                ReportMetadata.now()
        );
    }

    /**
     * Readiness trend data for the week.
     */
    public record ReadinessTrend(
            Map<LocalDate, Double> dailyScores,
            TrendDirection overallTrend,
            double weeklyAverage
    ) {
        public ReadinessTrend {
            if (dailyScores == null) dailyScores = Map.of();
        }

        public static ReadinessTrend empty() {
            return new ReadinessTrend(Map.of(), TrendDirection.STABLE, 0.0);
        }

        public static ReadinessTrend fromMap(Map<LocalDate, Double> scores) {
            if (scores == null || scores.isEmpty()) {
                return empty();
            }

            double avg = scores.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);

            TrendDirection direction = TrendDirection.STABLE;
            if (scores.size() >= 2) {
                double first = scores.values().iterator().next();
                double last = 0.0;
                for (double value : scores.values()) {
                    last = value;
                }
                double diff = last - first;
                if (diff > 5) direction = TrendDirection.IMPROVING;
                else if (diff < -5) direction = TrendDirection.DECLINING;
            }

            return new ReadinessTrend(scores, direction, avg);
        }
    }

    /**
     * Direction of the readiness trend.
     */
    public enum TrendDirection {
        IMPROVING, STABLE, DECLINING
    }

    /**
     * Compliance summary data.
     */
    public record ComplianceSummary(
            double completionPercent,
            double keySessionCompletionPercent,
            double zoneDistributionAdherencePercent,
            double unplannedLoadMinutes,
            List<String> flags
    ) {
        public ComplianceSummary {
            if (flags == null) flags = List.of();
            if (completionPercent < 0 || completionPercent > 100) {
                throw new IllegalArgumentException("Completion percent must be between 0 and 100");
            }
            if (keySessionCompletionPercent < 0 || keySessionCompletionPercent > 100) {
                throw new IllegalArgumentException("Key session completion percent must be between 0 and 100");
            }
            if (zoneDistributionAdherencePercent < 0 || zoneDistributionAdherencePercent > 100) {
                throw new IllegalArgumentException("Zone adherence percent must be between 0 and 100");
            }
            if (unplannedLoadMinutes < 0) {
                throw new IllegalArgumentException("Unplanned load minutes cannot be negative");
            }
        }

        public static ComplianceSummary empty() {
            return new ComplianceSummary(0.0, 0.0, 0.0, 0.0, List.of());
        }
    }

    /**
     * Report metadata including generation timestamp and version.
     */
    public record ReportMetadata(
            LocalDateTime generatedAt,
            String reportVersion,
            String dataSource
    ) {
        public static ReportMetadata now() {
            return new ReportMetadata(LocalDateTime.now(), "1.0", "training-coach");
        }

        public static ReportMetadata now(String dataSource) {
            return new ReportMetadata(LocalDateTime.now(), "1.0", dataSource);
        }
    }
}
