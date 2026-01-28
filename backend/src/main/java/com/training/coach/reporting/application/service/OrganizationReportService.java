package com.training.coach.reporting.application.service;

import com.training.coach.athlete.application.port.out.AthleteRepository;
import com.training.coach.reporting.domain.model.OrganizationReport;
import com.training.coach.shared.functional.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for generating organization-level reports for admin use.
 */
@Service
public class OrganizationReportService {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationReportService.class);

    private final AthleteRepository athleteRepository;
    private final WeeklyReportService weeklyReportService;

    public OrganizationReportService(
            AthleteRepository athleteRepository,
            WeeklyReportService weeklyReportService) {
        this.athleteRepository = athleteRepository;
        this.weeklyReportService = weeklyReportService;
    }

    /**
     * Generate an organization summary report for the given date range.
     */
    public Result<OrganizationReport> getOrganizationReport(String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            var athletes = athleteRepository.findAll();

            if (athletes.isEmpty()) {
                return Result.success(new OrganizationReport(0, 0, 0.0, 0.0, List.of()));
            }

            int totalAthletes = athletes.size();
            int activeAthletes = 0;
            double totalReadiness = 0.0;
            double totalCompliance = 0.0;

            var athleteSummaries = athletes.stream()
                .map(athlete -> {
                    try {
                        // Get weekly report for this athlete
                        var report = weeklyReportService.generateWeeklyReport(
                            athlete.id(),
                            start,
                            end
                        );

                        if (report != null) {
                            activeAthletes++;
                            totalReadiness += report.averageReadiness();
                            totalCompliance += report.compliance().completionPercent();

                            return new OrganizationReport.AthleteSummary(
                                athlete.id(),
                                athlete.name(),
                                report.averageReadiness(),
                                report.compliance().completionPercent(),
                                report.completedActivities().size()
                            );
                        } else {
                            return new OrganizationReport.AthleteSummary(
                                athlete.id(),
                                athlete.name(),
                                0.0,
                                0.0,
                                0
                            );
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to generate report for athlete {}", athlete.id(), e);
                        return new OrganizationReport.AthleteSummary(
                            athlete.id(),
                            athlete.name(),
                            0.0,
                            0.0,
                            0
                        );
                    }
                })
                .toList();

            double avgReadiness = activeAthletes > 0 ? totalReadiness / activeAthletes : 0.0;
            double avgCompliance = activeAthletes > 0 ? totalCompliance / activeAthletes : 0.0;

            logger.info("Generated organization report: {} athletes, {} active", totalAthletes, activeAthletes);

            return Result.success(new OrganizationReport(
                totalAthletes,
                activeAthletes,
                avgReadiness,
                avgCompliance,
                athleteSummaries
            ));

        } catch (Exception e) {
            logger.error("Failed to generate organization report", e);
            return Result.failure(e);
        }
    }
}
