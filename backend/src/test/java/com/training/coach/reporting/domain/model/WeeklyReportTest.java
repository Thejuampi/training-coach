package com.training.coach.reporting.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeeklyReportTest {

    @Test
    void shouldCreateWeeklyReportWithValidData() {
        LocalDate weekStart = LocalDate.of(2026, 1, 1);
        LocalDate weekEnd = LocalDate.of(2026, 1, 7);

        Map<LocalDate, Double> readinessScores = Map.of(
                weekStart, 75.0,
                weekStart.plusDays(1), 72.0,
                weekStart.plusDays(2), 68.0,
                weekStart.plusDays(3), 70.0,
                weekStart.plusDays(4), 78.0,
                weekStart.plusDays(5), 80.0,
                weekStart.plusDays(6), 82.0
        );

        WeeklyReport.ReadinessTrend readinessTrend =
                WeeklyReport.ReadinessTrend.fromMap(readinessScores);

        WeeklyReport.ComplianceSummary compliance = new WeeklyReport.ComplianceSummary(
                85.0,
                90.0,
                92.0,
                45.0,
                List.of("Missed Tuesday threshold session")
        );

        List<String> keyNotes = List.of(
                "Strong endurance block on Thursday",
                "HRV dipped mid-week but recovered"
        );

        WeeklyReport report = WeeklyReport.create(
                "athlete-123",
                "John Doe",
                weekStart,
                weekEnd,
                readinessTrend,
                compliance,
                keyNotes,
                List.of()
        );

        assertThat(report.id()).isNotEmpty();
        assertThat(report.athleteId()).isEqualTo("athlete-123");
        assertThat(report.athleteName()).isEqualTo("John Doe");
        assertThat(report.weekStart()).isEqualTo(weekStart);
        assertThat(report.weekEnd()).isEqualTo(weekEnd);
        assertThat(report.numberOfDays()).isEqualTo(7);
    }

    @Test
    void shouldCalculateAverageReadiness() {
        LocalDate weekStart = LocalDate.of(2026, 1, 1);
        LocalDate weekEnd = LocalDate.of(2026, 1, 7);

        Map<LocalDate, Double> readinessScores = Map.of(
                weekStart, 75.0,
                weekStart.plusDays(1), 72.0,
                weekStart.plusDays(2), 68.0
        );

        WeeklyReport.ReadinessTrend readinessTrend =
                WeeklyReport.ReadinessTrend.fromMap(readinessScores);

        WeeklyReport report = WeeklyReport.create(
                "athlete-123",
                "John Doe",
                weekStart,
                weekEnd,
                readinessTrend,
                WeeklyReport.ComplianceSummary.empty(),
                List.of(),
                List.of()
        );

        assertThat(report.averageReadiness()).isEqualTo((75.0 + 72.0 + 68.0) / 3);
    }

    @Test
    void shouldDetectImprovingTrend() {
        LocalDate weekStart = LocalDate.of(2026, 1, 1);
        LocalDate weekEnd = LocalDate.of(2026, 1, 7);

        Map<LocalDate, Double> readinessScores = Map.of(
                weekStart, 60.0,
                weekStart.plusDays(3), 75.0,
                weekStart.plusDays(6), 85.0
        );

        WeeklyReport.ReadinessTrend readinessTrend =
                WeeklyReport.ReadinessTrend.fromMap(readinessScores);

        assertThat(readinessTrend.overallTrend())
                .isEqualTo(WeeklyReport.TrendDirection.IMPROVING);
    }

    @Test
    void shouldDetectDecliningTrend() {
        LocalDate weekStart = LocalDate.of(2026, 1, 1);
        LocalDate weekEnd = LocalDate.of(2026, 1, 7);

        Map<LocalDate, Double> readinessScores = Map.of(
                weekStart, 85.0,
                weekStart.plusDays(3), 75.0,
                weekStart.plusDays(6), 60.0
        );

        WeeklyReport.ReadinessTrend readinessTrend =
                WeeklyReport.ReadinessTrend.fromMap(readinessScores);

        assertThat(readinessTrend.overallTrend())
                .isEqualTo(WeeklyReport.TrendDirection.DECLINING);
    }

    @Test
    void shouldDetectStableTrend() {
        LocalDate weekStart = LocalDate.of(2026, 1, 1);
        LocalDate weekEnd = LocalDate.of(2026, 1, 7);

        Map<LocalDate, Double> readinessScores = Map.of(
                weekStart, 75.0,
                weekStart.plusDays(3), 76.0,
                weekStart.plusDays(6), 75.0
        );

        WeeklyReport.ReadinessTrend readinessTrend =
                WeeklyReport.ReadinessTrend.fromMap(readinessScores);

        assertThat(readinessTrend.overallTrend())
                .isEqualTo(WeeklyReport.TrendDirection.STABLE);
    }

    @Test
    void shouldThrowExceptionWhenAthleteIdIsNull() {
        LocalDate weekStart = LocalDate.of(2026, 1, 1);
        LocalDate weekEnd = LocalDate.of(2026, 1, 7);

        assertThatThrownBy(() -> new WeeklyReport(
                "report-123",
                null,
                "John Doe",
                weekStart,
                weekEnd,
                WeeklyReport.ReadinessTrend.empty(),
                WeeklyReport.ComplianceSummary.empty(),
                List.of(),
                List.of(),
                WeeklyReport.ReportMetadata.now()
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Athlete ID cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenWeekStartIsAfterWeekEnd() {
        LocalDate weekStart = LocalDate.of(2026, 1, 7);
        LocalDate weekEnd = LocalDate.of(2026, 1, 1);

        assertThatThrownBy(() -> WeeklyReport.create(
                "athlete-123",
                "John Doe",
                weekStart,
                weekEnd,
                WeeklyReport.ReadinessTrend.empty(),
                WeeklyReport.ComplianceSummary.empty(),
                List.of(),
                List.of()
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Week start must be before or equal to week end");
    }

    @Test
    void shouldThrowExceptionWhenCompletionPercentIsInvalid() {
        assertThatThrownBy(() -> new WeeklyReport.ComplianceSummary(
                150.0,
                90.0,
                92.0,
                45.0,
                List.of()
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Completion percent must be between 0 and 100");
    }

    @Test
    void shouldThrowExceptionWhenUnplannedLoadMinutesIsNegative() {
        assertThatThrownBy(() -> new WeeklyReport.ComplianceSummary(
                85.0,
                90.0,
                92.0,
                -10.0,
                List.of()
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unplanned load minutes cannot be negative");
    }

    @Test
    void shouldHandleEmptyReadinessTrend() {
        WeeklyReport.ReadinessTrend emptyTrend = WeeklyReport.ReadinessTrend.empty();

        assertThat(emptyTrend.dailyScores()).isEmpty();
        assertThat(emptyTrend.overallTrend())
                .isEqualTo(WeeklyReport.TrendDirection.STABLE);
        assertThat(emptyTrend.weeklyAverage()).isEqualTo(0.0);
    }

    @Test
    void shouldHandleEmptyComplianceSummary() {
        WeeklyReport.ComplianceSummary empty = WeeklyReport.ComplianceSummary.empty();

        assertThat(empty.completionPercent()).isEqualTo(0.0);
        assertThat(empty.keySessionCompletionPercent()).isEqualTo(0.0);
        assertThat(empty.zoneDistributionAdherencePercent()).isEqualTo(0.0);
        assertThat(empty.unplannedLoadMinutes()).isEqualTo(0.0);
        assertThat(empty.flags()).isEmpty();
    }
}
