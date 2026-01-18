package com.training.coach.wellness.application.service;

import com.training.coach.wellness.application.port.out.WellnessRepository;
import com.training.coach.wellness.domain.model.WellnessInsights;
import com.training.coach.wellness.domain.model.WellnessReport;
import com.training.coach.wellness.domain.model.WellnessSnapshot;
import com.training.coach.wellness.domain.model.WellnessTrends;
import com.training.coach.wellness.presentation.WellnessController.WellnessDashboardResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WellnessReportService {

    private final WellnessRepository wellnessRepository;
    private final TrendCalculationService trendService;
    private final WellnessInsightsService insightsService;

    public WellnessReportService(
            WellnessRepository wellnessRepository,
            TrendCalculationService trendService,
            WellnessInsightsService insightsService) {
        this.wellnessRepository = wellnessRepository;
        this.trendService = trendService;
        this.insightsService = insightsService;
    }

    public WellnessReport generateWellnessReport(String athleteId, LocalDate startDate, LocalDate endDate) {
        List<WellnessSnapshot> snapshots =
                wellnessRepository.findByAthleteIdAndDateRange(athleteId, startDate, endDate);

        WellnessTrends trends = trendService.calculateTrends(snapshots);

        WellnessInsights insights = insightsService.generateInsights(snapshots, trends, 0, 0);

        return new WellnessReport(
                athleteId, startDate, endDate, snapshots, trends, insights, WellnessReport.ReportMetadata.empty());
    }

    public WellnessDashboardResponse generateDashboard(String athleteId, LocalDate startDate, LocalDate endDate) {
        List<WellnessSnapshot> snapshots =
                wellnessRepository.findByAthleteIdAndDateRange(athleteId, startDate, endDate);

        if (snapshots.isEmpty()) {
            return new WellnessDashboardResponse(
                    athleteId, 0.0, "no_data", 0.0, 0.0, 0.0, 0.0, 0, List.of(), List.of(), LocalDate.now());
        }

        WellnessSnapshot latest = snapshots.get(snapshots.size() - 1);
        WellnessTrends trends = trendService.calculateTrends(snapshots);
        WellnessInsights insights = insightsService.generateInsights(snapshots, trends, 0, 0);

        double avgReadiness = snapshots.stream()
                .mapToDouble(WellnessSnapshot::readinessScore)
                .average()
                .orElse(0.0);
        double avgHrv = snapshots.stream()
                .filter(s -> s.physiological() != null && s.physiological().hrv() != null)
                .mapToDouble(s -> s.physiological().hrv().value())
                .average()
                .orElse(0.0);
        double avgRhr = snapshots.stream()
                .filter(s -> s.physiological() != null && s.physiological().restingHeartRate() != null)
                .mapToDouble(s -> s.physiological().restingHeartRate().value())
                .average()
                .orElse(0.0);
        double avgSleep = snapshots.stream()
                .filter(s -> s.physiological() != null && s.physiological().sleep() != null)
                .mapToDouble(s -> s.physiological().sleep().totalSleepHours().value())
                .average()
                .orElse(0.0);

        int days = (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
        int coveragePercent = (int) ((double) snapshots.size() / days * 100);

        String trend = trends.readinessTrend().name().toLowerCase();

        return new WellnessDashboardResponse(
                athleteId,
                latest.readinessScore(),
                trend,
                avgReadiness,
                avgHrv,
                avgRhr,
                avgSleep,
                coveragePercent,
                insights.flags(),
                insights.achievements(),
                LocalDate.now());
    }
}
