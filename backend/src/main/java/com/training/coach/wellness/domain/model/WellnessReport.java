package com.training.coach.wellness.domain.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public record WellnessReport(
        String athleteId,
        LocalDate reportStartDate,
        LocalDate reportEndDate,
        List<WellnessSnapshot> dailySnapshots,
        WellnessTrends trends,
        WellnessInsights insights,
        ReportMetadata metadata) {
    public WellnessReport {
        if (athleteId == null || athleteId.isBlank()) {
            throw new IllegalArgumentException("Athlete ID cannot be null or blank");
        }
        if (reportStartDate == null || reportEndDate == null) {
            throw new IllegalArgumentException("Report dates cannot be null");
        }
        if (reportStartDate.isAfter(reportEndDate)) {
            throw new IllegalArgumentException("Report start date must be before or equal to end date");
        }
        if (dailySnapshots == null) dailySnapshots = List.of();
        if (trends == null) trends = WellnessTrends.empty();
        if (insights == null) insights = WellnessInsights.empty();
        if (metadata == null) metadata = ReportMetadata.empty();
    }

    public int numberOfDays() {
        return (int) (reportEndDate.toEpochDay() - reportStartDate.toEpochDay()) + 1;
    }

    public int numberOfSnapshots() {
        return dailySnapshots.size();
    }

    public double snapshotCoverage() {
        return numberOfDays() > 0 ? (double) numberOfSnapshots() / numberOfDays() * 100 : 0;
    }

    public record ReportMetadata(LocalDate generatedAt, String reportVersion, String dataSource) {
        public static ReportMetadata empty() {
            return new ReportMetadata(LocalDate.now(), "1.0", "intervals.icu");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WellnessReport that = (WellnessReport) o;
        return Objects.equals(athleteId, that.athleteId)
                && Objects.equals(reportStartDate, that.reportStartDate)
                && Objects.equals(reportEndDate, that.reportEndDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(athleteId, reportStartDate, reportEndDate);
    }
}
