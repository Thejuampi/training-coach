package com.training.coach.reporting.application.service;

import com.training.coach.athlete.application.port.out.AthleteRepository;
import com.training.coach.athlete.domain.model.Athlete;
import com.training.coach.reporting.application.port.out.WeeklyReportRepository;
import com.training.coach.reporting.domain.model.WeeklyReport;
import com.training.coach.wellness.application.port.out.WellnessRepository;
import com.training.coach.wellness.domain.model.WellnessSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for generating weekly training reports.
 */
@Service
public class WeeklyReportService {

    private static final Logger logger = LoggerFactory.getLogger(WeeklyReportService.class);

    private final WeeklyReportRepository reportRepository;
    private final AthleteRepository athleteRepository;
    private final WellnessRepository wellnessRepository;

    public WeeklyReportService(
            WeeklyReportRepository reportRepository,
            AthleteRepository athleteRepository,
            WellnessRepository wellnessRepository) {
        this.reportRepository = reportRepository;
        this.athleteRepository = athleteRepository;
        this.wellnessRepository = wellnessRepository;
    }

    /**
     * Generate a weekly report for an athlete.
     */
    public WeeklyReport generateWeeklyReport(String athleteId, LocalDate weekStart, LocalDate weekEnd) {
        Athlete athlete = athleteRepository.findById(athleteId)
                .orElseThrow(() -> new IllegalArgumentException("Athlete not found: " + athleteId));

        // Get wellness data for the week
        List<WellnessSnapshot> snapshots = wellnessRepository.findByAthleteIdAndDateRange(
                athleteId, weekStart, weekEnd);

        // Build readiness trend map
        Map<LocalDate, Double> readinessTrendMap = snapshots.stream()
                .collect(Collectors.toMap(
                        WellnessSnapshot::date,
                        WellnessSnapshot::readinessScore,
                        (existing, replacement) -> existing
                ));

        // Create readiness trend
        WeeklyReport.ReadinessTrend readinessTrend =
                WeeklyReport.ReadinessTrend.fromMap(readinessTrendMap);

        // Create compliance summary (defaults for now - can be enhanced with activity data)
        WeeklyReport.ComplianceSummary compliance = WeeklyReport.ComplianceSummary.empty();

        // Generate key notes based on wellness data
        List<String> keyNotes = generateKeyNotes(snapshots, readinessTrend);

        // Get completed activities (placeholder - would integrate with activity service)
        List<String> completedActivities = List.of();

        WeeklyReport report = WeeklyReport.create(
                athleteId,
                athlete.name(),
                weekStart,
                weekEnd,
                readinessTrend,
                compliance,
                keyNotes,
                completedActivities
        );

        WeeklyReport saved = reportRepository.save(report);
        logger.info("Generated weekly report for athlete {} for week {} to {}",
                athleteId, weekStart, weekEnd);
        return saved;
    }

    /**
     * Generate a weekly report with custom data.
     */
    public WeeklyReport generateWeeklyReportWithData(
            String athleteId,
            String athleteName,
            LocalDate weekStart,
            LocalDate weekEnd,
            Map<LocalDate, Double> readinessTrends,
            WeeklyReport.ComplianceSummary compliance,
            List<String> keyNotes,
            List<String> completedActivities
    ) {
        WeeklyReport.ReadinessTrend readinessTrend =
                WeeklyReport.ReadinessTrend.fromMap(readinessTrends);

        WeeklyReport report = WeeklyReport.create(
                athleteId,
                athleteName,
                weekStart,
                weekEnd,
                readinessTrend,
                compliance,
                keyNotes,
                completedActivities
        );

        return reportRepository.save(report);
    }

    /**
     * Get a report by ID.
     */
    public WeeklyReport getReport(String reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
    }

    /**
     * Get all reports for an athlete.
     */
    public List<WeeklyReport> getReportsForAthlete(String athleteId) {
        return reportRepository.findByAthleteId(athleteId);
    }

    /**
     * Get the most recent report for an athlete.
     */
    public WeeklyReport getMostRecentReport(String athleteId) {
        return reportRepository.findMostRecentByAthleteId(athleteId)
                .orElseThrow(() -> new IllegalArgumentException("No report found for athlete: " + athleteId));
    }

    /**
     * Get reports for a date range.
     */
    public List<WeeklyReport> getReportsForDateRange(String athleteId, LocalDate startDate, LocalDate endDate) {
        return reportRepository.findByAthleteIdAndDateRange(athleteId, startDate, endDate);
    }

    /**
     * Delete a report.
     */
    public void deleteReport(String reportId) {
        reportRepository.delete(reportId);
        logger.info("Deleted weekly report: {}", reportId);
    }

    /**
     * Generate key notes based on wellness data and trends.
     */
    private List<String> generateKeyNotes(
            List<WellnessSnapshot> snapshots,
            WeeklyReport.ReadinessTrend readinessTrend) {

        if (snapshots.isEmpty()) {
            return List.of("No wellness data available for this week");
        }

        List<String> notes = new java.util.ArrayList<>();

        // Add trend note
        switch (readinessTrend.overallTrend()) {
            case IMPROVING -> notes.add("Readiness trending upward throughout the week");
            case DECLINING -> notes.add("Readiness declining - consider recovery focus");
            case STABLE -> notes.add("Readiness remained stable this week");
        }

        // Add average readiness note
        double avgReadiness = readinessTrend.weeklyAverage();
        if (avgReadiness >= 80) {
            notes.add("High readiness scores indicate good adaptation to training");
        } else if (avgReadiness >= 60) {
            notes.add("Moderate readiness - current training load is appropriate");
        } else {
            notes.add("Low readiness detected - monitor for overreaching");
        }

        // Add HRV notes if available
        long hrvAvailable = snapshots.stream()
                .filter(s -> s.physiological() != null && s.physiological().hrv() != null)
                .count();
        if (hrvAvailable > 0) {
            notes.add(String.format("HRV data available for %d of %d days",
                    hrvAvailable, snapshots.size()));
        }

        return notes;
    }
}
