package com.training.coach.reporting.application.service;

import com.training.coach.reporting.application.port.out.WeeklyReportRepository;
import com.training.coach.reporting.domain.model.WeeklyReport;
import com.training.coach.testconfig.inmemory.InMemoryWeeklyReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeeklyReportServiceTest {

    private WeeklyReportRepository repository;
    private WeeklyReportService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryWeeklyReportRepository();
        service = new WeeklyReportService(repository, null, null);
    }

    @Test
    void shouldGenerateWeeklyReportWithData() {
        LocalDate weekStart = LocalDate.of(2026, 1, 1);
        LocalDate weekEnd = LocalDate.of(2026, 1, 7);

        Map<LocalDate, Double> readinessTrends = Map.of(
                weekStart, 75.0,
                weekStart.plusDays(1), 72.0,
                weekStart.plusDays(2), 68.0,
                weekStart.plusDays(3), 70.0,
                weekStart.plusDays(4), 78.0,
                weekStart.plusDays(5), 80.0,
                weekStart.plusDays(6), 82.0
        );

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

        List<String> completedActivities = List.of(
                "2026-01-01: Endurance Ride",
                "2026-01-03: Interval Session"
        );

        WeeklyReport report = service.generateWeeklyReportWithData(
                "athlete-123",
                "John Doe",
                weekStart,
                weekEnd,
                readinessTrends,
                compliance,
                keyNotes,
                completedActivities
        );

        assertThat(report).isNotNull();
        assertThat(report.athleteId()).isEqualTo("athlete-123");
        assertThat(report.athleteName()).isEqualTo("John Doe");
        assertThat(report.weekStart()).isEqualTo(weekStart);
        assertThat(report.weekEnd()).isEqualTo(weekEnd);
        assertThat(report.keyNotes()).hasSize(2);
        assertThat(report.completedActivities()).hasSize(2);
        assertThat(report.readinessTrend().overallTrend())
                .isEqualTo(WeeklyReport.TrendDirection.IMPROVING);
    }

    @Test
    void shouldRetrieveReportById() {
        LocalDate weekStart = LocalDate.of(2026, 1, 1);
        LocalDate weekEnd = LocalDate.of(2026, 1, 7);

        WeeklyReport report = service.generateWeeklyReportWithData(
                "athlete-123",
                "John Doe",
                weekStart,
                weekEnd,
                Map.of(weekStart, 75.0),
                WeeklyReport.ComplianceSummary.empty(),
                List.of(),
                List.of()
        );

        WeeklyReport retrieved = service.getReport(report.id());

        assertThat(retrieved.id()).isEqualTo(report.id());
        assertThat(retrieved.athleteId()).isEqualTo("athlete-123");
    }

    @Test
    void shouldRetrieveReportsForAthlete() {
        LocalDate week1Start = LocalDate.of(2026, 1, 1);
        LocalDate week1End = LocalDate.of(2026, 1, 7);
        LocalDate week2Start = LocalDate.of(2026, 1, 8);
        LocalDate week2End = LocalDate.of(2026, 1, 14);

        service.generateWeeklyReportWithData(
                "athlete-123",
                "John Doe",
                week1Start,
                week1End,
                Map.of(week1Start, 75.0),
                WeeklyReport.ComplianceSummary.empty(),
                List.of(),
                List.of()
        );

        service.generateWeeklyReportWithData(
                "athlete-123",
                "John Doe",
                week2Start,
                week2End,
                Map.of(week2Start, 78.0),
                WeeklyReport.ComplianceSummary.empty(),
                List.of(),
                List.of()
        );

        List<WeeklyReport> reports = service.getReportsForAthlete("athlete-123");

        assertThat(reports).hasSize(2);
    }

    @Test
    void shouldRetrieveMostRecentReportForAthlete() {
        LocalDate week1Start = LocalDate.of(2026, 1, 1);
        LocalDate week1End = LocalDate.of(2026, 1, 7);
        LocalDate week2Start = LocalDate.of(2026, 1, 8);
        LocalDate week2End = LocalDate.of(2026, 1, 14);

        service.generateWeeklyReportWithData(
                "athlete-123",
                "John Doe",
                week1Start,
                week1End,
                Map.of(week1Start, 75.0),
                WeeklyReport.ComplianceSummary.empty(),
                List.of(),
                List.of()
        );

        service.generateWeeklyReportWithData(
                "athlete-123",
                "John Doe",
                week2Start,
                week2End,
                Map.of(week2Start, 78.0),
                WeeklyReport.ComplianceSummary.empty(),
                List.of(),
                List.of()
        );

        WeeklyReport mostRecent = service.getMostRecentReport("athlete-123");

        assertThat(mostRecent.weekStart()).isEqualTo(week2Start);
    }

    @Test
    void shouldRetrieveReportsForDateRange() {
        LocalDate week1Start = LocalDate.of(2026, 1, 1);
        LocalDate week1End = LocalDate.of(2026, 1, 7);
        LocalDate week2Start = LocalDate.of(2026, 1, 15);
        LocalDate week2End = LocalDate.of(2026, 1, 21);

        service.generateWeeklyReportWithData(
                "athlete-123",
                "John Doe",
                week1Start,
                week1End,
                Map.of(week1Start, 75.0),
                WeeklyReport.ComplianceSummary.empty(),
                List.of(),
                List.of()
        );

        service.generateWeeklyReportWithData(
                "athlete-123",
                "John Doe",
                week2Start,
                week2End,
                Map.of(week2Start, 78.0),
                WeeklyReport.ComplianceSummary.empty(),
                List.of(),
                List.of()
        );

        List<WeeklyReport> reports = service.getReportsForDateRange(
                "athlete-123",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 10)
        );

        assertThat(reports).hasSize(1);
        assertThat(reports.get(0).weekStart()).isEqualTo(week1Start);
    }

    @Test
    void shouldDeleteReport() {
        LocalDate weekStart = LocalDate.of(2026, 1, 1);
        LocalDate weekEnd = LocalDate.of(2026, 1, 7);

        WeeklyReport report = service.generateWeeklyReportWithData(
                "athlete-123",
                "John Doe",
                weekStart,
                weekEnd,
                Map.of(weekStart, 75.0),
                WeeklyReport.ComplianceSummary.empty(),
                List.of(),
                List.of()
        );

        service.deleteReport(report.id());

        assertThatThrownBy(() -> service.getReport(report.id()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Report not found");
    }

    @Test
    void shouldThrowExceptionWhenReportNotFound() {
        assertThatThrownBy(() -> service.getReport("non-existent-id"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Report not found");
    }
}
