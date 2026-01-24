package com.training.coach.testconfig.inmemory;

import com.training.coach.reporting.application.port.out.WeeklyReportRepository;
import com.training.coach.reporting.domain.model.WeeklyReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory WeeklyReportRepository for fast tests.
 */
public class InMemoryWeeklyReportRepository implements WeeklyReportRepository {
    private final ConcurrentHashMap<String, WeeklyReport> reports = new ConcurrentHashMap<>();

    @Override
    public WeeklyReport save(WeeklyReport report) {
        reports.put(report.id(), report);
        return report;
    }

    @Override
    public Optional<WeeklyReport> findById(String reportId) {
        return Optional.ofNullable(reports.get(reportId));
    }

    @Override
    public List<WeeklyReport> findByAthleteId(String athleteId) {
        return reports.values().stream()
                .filter(r -> r.athleteId().equals(athleteId))
                .collect(Collectors.toList());
    }

    @Override
    public List<WeeklyReport> findByAthleteIdAndDateRange(String athleteId, LocalDate startDate, LocalDate endDate) {
        return reports.values().stream()
                .filter(r -> r.athleteId().equals(athleteId))
                .filter(r -> !r.weekStart().isBefore(startDate) && !r.weekEnd().isAfter(endDate))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<WeeklyReport> findMostRecentByAthleteId(String athleteId) {
        return reports.values().stream()
                .filter(r -> r.athleteId().equals(athleteId))
                .max((r1, r2) -> r1.weekEnd().compareTo(r2.weekEnd()));
    }

    @Override
    public Optional<WeeklyReport> findByAthleteIdAndWeek(String athleteId, LocalDate weekStart) {
        return reports.values().stream()
                .filter(r -> r.athleteId().equals(athleteId))
                .filter(r -> r.weekStart().equals(weekStart))
                .findFirst();
    }

    @Override
    public void delete(String reportId) {
        reports.remove(reportId);
    }

    /**
     * Clear all reports for testing purposes.
     */
    public void clearAll() {
        reports.clear();
    }
}
