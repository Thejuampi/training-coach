package com.training.coach.reporting.presentation;

import com.training.coach.reporting.application.service.WeeklyReportService;
import com.training.coach.reporting.domain.model.WeeklyReport;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller for weekly report operations.
 */
@RestController
@RequestMapping("/api/reports/weekly")
public class WeeklyReportController {

    private final WeeklyReportService reportService;

    public WeeklyReportController(WeeklyReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Generate a weekly report for an athlete.
     */
    @PostMapping("/generate")
    public ResponseEntity<WeeklyReport> generateReport(
            @RequestParam String athleteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEnd
    ) {
        WeeklyReport report = reportService.generateWeeklyReport(athleteId, weekStart, weekEnd);
        return ResponseEntity.ok(report);
    }

    /**
     * Generate a weekly report with custom data.
     */
    @PostMapping("/generate-with-data")
    public ResponseEntity<WeeklyReport> generateReportWithData(
            @RequestParam String athleteId,
            @RequestParam String athleteName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEnd,
            @RequestBody ReportDataRequest data
    ) {
        WeeklyReport report = reportService.generateWeeklyReportWithData(
                athleteId,
                athleteName,
                weekStart,
                weekEnd,
                data.readinessTrends(),
                data.compliance(),
                data.keyNotes(),
                data.completedActivities()
        );
        return ResponseEntity.ok(report);
    }

    /**
     * Get a report by ID.
     */
    @GetMapping("/{reportId}")
    public ResponseEntity<WeeklyReport> getReport(@PathVariable String reportId) {
        WeeklyReport report = reportService.getReport(reportId);
        return ResponseEntity.ok(report);
    }

    /**
     * Get all reports for an athlete.
     */
    @GetMapping("/athlete/{athleteId}")
    public ResponseEntity<List<WeeklyReport>> getReportsForAthlete(@PathVariable String athleteId) {
        List<WeeklyReport> reports = reportService.getReportsForAthlete(athleteId);
        return ResponseEntity.ok(reports);
    }

    /**
     * Get the most recent report for an athlete.
     */
    @GetMapping("/athlete/{athleteId}/most-recent")
    public ResponseEntity<WeeklyReport> getMostRecentReport(@PathVariable String athleteId) {
        WeeklyReport report = reportService.getMostRecentReport(athleteId);
        return ResponseEntity.ok(report);
    }

    /**
     * Get reports for a date range.
     */
    @GetMapping("/athlete/{athleteId}/range")
    public ResponseEntity<List<WeeklyReport>> getReportsForDateRange(
            @PathVariable String athleteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<WeeklyReport> reports = reportService.getReportsForDateRange(athleteId, startDate, endDate);
        return ResponseEntity.ok(reports);
    }

    /**
     * Export a report as CSV.
     */
    @GetMapping("/{reportId}/export/csv")
    public ResponseEntity<byte[]> exportAsCsv(@PathVariable String reportId) {
        WeeklyReport report = reportService.getReport(reportId);
        byte[] csvData = generateCsv(report);

        String filename = String.format("weekly_report_%s_%s.csv",
                report.athleteName().replace(" ", "_"),
                report.weekStart());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }

    /**
     * Export a report as JSON.
     */
    @GetMapping("/{reportId}/export/json")
    public ResponseEntity<byte[]> exportAsJson(@PathVariable String reportId) {
        WeeklyReport report = reportService.getReport(reportId);
        byte[] jsonData = generateJson(report);

        String filename = String.format("weekly_report_%s_%s.json",
                report.athleteName().replace(" ", "_"),
                report.weekStart());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonData);
    }

    /**
     * Delete a report.
     */
    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteReport(@PathVariable String reportId) {
        reportService.deleteReport(reportId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Generate CSV from a weekly report.
     */
    private byte[] generateCsv(WeeklyReport report) {
        StringBuilder csv = new StringBuilder();

        // Header
        csv.append("Training Coach - Weekly Report\n");
        csv.append("Athlete:,").append(report.athleteName()).append("\n");
        csv.append("Period:,").append(report.weekStart()).append(" to ").append(report.weekEnd()).append("\n\n");

        // Readiness Section
        csv.append("READINESS TREND\n");
        csv.append("Weekly Average,").append(String.format("%.1f", report.averageReadiness())).append("\n");
        csv.append("Trend,").append(report.readinessTrend().overallTrend().name()).append("\n\n");

        csv.append("Daily Readiness\n");
        csv.append("Date,Readiness Score\n");
        for (Map.Entry<LocalDate, Double> entry : report.readinessTrend().dailyScores().entrySet()) {
            csv.append(entry.getKey()).append(",");
            csv.append(String.format("%.1f", entry.getValue())).append("\n");
        }
        csv.append("\n");

        // Compliance Section
        csv.append("COMPLIANCE SUMMARY\n");
        csv.append("Completion Rate,").append(String.format("%.1f%%",
                report.compliance().completionPercent())).append("\n");
        csv.append("Key Session Completion,").append(String.format("%.1f%%",
                report.compliance().keySessionCompletionPercent())).append("\n");
        csv.append("Zone Distribution Adherence,").append(String.format("%.1f%%",
                report.compliance().zoneDistributionAdherencePercent())).append("\n");
        csv.append("Unplanned Load (min),").append(String.format("%.0f",
                report.compliance().unplannedLoadMinutes())).append("\n\n");

        // Flags
        if (!report.compliance().flags().isEmpty()) {
            csv.append("FLAGS\n");
            for (String flag : report.compliance().flags()) {
                csv.append("- ").append(flag).append("\n");
            }
            csv.append("\n");
        }

        // Key Notes
        if (!report.keyNotes().isEmpty()) {
            csv.append("KEY NOTES\n");
            for (String note : report.keyNotes()) {
                csv.append("- ").append(note).append("\n");
            }
            csv.append("\n");
        }

        // Completed Activities
        if (!report.completedActivities().isEmpty()) {
            csv.append("COMPLETED ACTIVITIES\n");
            csv.append("Activity Details\n");
            for (String activity : report.completedActivities()) {
                csv.append(activity).append("\n");
            }
        }

        return csv.toString().getBytes();
    }

    /**
     * Generate JSON from a weekly report.
     */
    private byte[] generateJson(WeeklyReport report) {
        StringBuilder json = new StringBuilder();

        json.append("{\n");
        json.append("  \"report\": {\n");
        json.append("    \"id\": \"").append(report.id()).append("\",\n");
        json.append("    \"type\": \"weekly\",\n");
        json.append("    \"athlete\": {\n");
        json.append("      \"id\": \"").append(report.athleteId()).append("\",\n");
        json.append("      \"name\": \"").append(report.athleteName()).append("\"\n");
        json.append("    },\n");
        json.append("    \"period\": {\n");
        json.append("      \"start\": \"").append(report.weekStart()).append("\",\n");
        json.append("      \"end\": \"").append(report.weekEnd()).append("\"\n");
        json.append("    },\n");

        // Readiness section
        json.append("    \"readiness\": {\n");
        json.append("      \"weeklyAverage\": ").append(String.format("%.2f", report.averageReadiness())).append(",\n");
        json.append("      \"trend\": \"").append(report.readinessTrend().overallTrend().name()).append("\",\n");
        json.append("      \"dailyScores\": {\n");
        int scoreCount = 0;
        for (Map.Entry<LocalDate, Double> entry : report.readinessTrend().dailyScores().entrySet()) {
            if (scoreCount > 0) json.append(",\n");
            json.append("        \"").append(entry.getKey()).append("\": ")
                    .append(String.format("%.2f", entry.getValue()));
            scoreCount++;
        }
        json.append("\n      }\n");
        json.append("    },\n");

        // Compliance section
        json.append("    \"compliance\": {\n");
        json.append("      \"completionRate\": ")
                .append(String.format("%.2f", report.compliance().completionPercent())).append(",\n");
        json.append("      \"keySessionCompletion\": ")
                .append(String.format("%.2f", report.compliance().keySessionCompletionPercent())).append(",\n");
        json.append("      \"zoneDistributionAdherence\": ")
                .append(String.format("%.2f", report.compliance().zoneDistributionAdherencePercent())).append(",\n");
        json.append("      \"unplannedLoadMinutes\": ")
                .append(report.compliance().unplannedLoadMinutes()).append(",\n");
        json.append("      \"flags\": [");
        if (!report.compliance().flags().isEmpty()) {
            for (int i = 0; i < report.compliance().flags().size(); i++) {
                if (i > 0) json.append(", ");
                json.append("\"").append(report.compliance().flags().get(i)).append("\"");
            }
        }
        json.append("]\n");
        json.append("  },\n");

        // Key notes
        json.append("    \"keyNotes\": [");
        if (!report.keyNotes().isEmpty()) {
            for (int i = 0; i < report.keyNotes().size(); i++) {
                if (i > 0) json.append(", ");
                json.append("\"").append(escapeJson(report.keyNotes().get(i))).append("\"");
            }
        }
        json.append("],\n");

        // Completed activities
        json.append("    \"completedActivities\": [");
        if (!report.completedActivities().isEmpty()) {
            for (int i = 0; i < report.completedActivities().size(); i++) {
                if (i > 0) json.append(", ");
                json.append("\"").append(escapeJson(report.completedActivities().get(i))).append("\"");
            }
        }
        json.append("],\n");

        // Metadata
        json.append("    \"metadata\": {\n");
        json.append("      \"generatedAt\": \"").append(report.metadata().generatedAt()).append("\",\n");
        json.append("      \"version\": \"").append(report.metadata().reportVersion()).append("\",\n");
        json.append("      \"dataSource\": \"").append(report.metadata().dataSource()).append("\"\n");
        json.append("    }\n");

        json.append("  }\n");
        json.append("}\n");

        return json.toString().getBytes();
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Request body for generating report with custom data.
     */
    public record ReportDataRequest(
            Map<LocalDate, Double> readinessTrends,
            WeeklyReport.ComplianceSummary compliance,
            List<String> keyNotes,
            List<String> completedActivities
    ) {}
}
