package com.training.coach.analysis.application.service;

import com.training.coach.analysis.domain.model.ComplianceSummary;
import com.training.coach.analysis.domain.model.ProgressSummary;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Service for exporting reports in various formats.
 */
@Service
public class ExportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Export weekly report as CSV.
     */
    public byte[] exportWeeklyReport(
            String athleteName,
            LocalDate weekStart,
            LocalDate weekEnd,
            ComplianceSummary compliance,
            Map<LocalDate, Double> readinessTrends,
            List<String> completedActivities) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StringBuilder csv = new StringBuilder();

        // Header
        csv.append("Training Coach - Weekly Report\n");
        csv.append("Athlete:,").append(athleteName).append("\n");
        csv.append("Period:,").append(weekStart).append(" to ").append(weekEnd).append("\n\n");

        // Compliance Section
        csv.append("COMPLIANCE SUMMARY\n");
        csv.append("Completion Rate,").append(String.format("%.1f%%", compliance.completionPercent())).append("\n");
        csv.append("Key Session Completion,").append(String.format("%.1f%%", compliance.keySessionCompletionPercent())).append("\n");
        csv.append("Zone Distribution Adherence,").append(String.format("%.1f%%", compliance.zoneDistributionAdherencePercent())).append("\n");
        csv.append("Unplanned Load (min),").append(String.format("%.0f", compliance.unplannedLoadMinutes())).append("\n\n");

        // Flags
        if (!compliance.flags().isEmpty()) {
            csv.append("FLAGS\n");
            for (String flag : compliance.flags()) {
                csv.append("- ").append(flag).append("\n");
            }
            csv.append("\n");
        }

        // Readiness Trends
        csv.append("READINESS TRENDS\n");
        csv.append("Date,Readiness Score\n");
        for (Map.Entry<LocalDate, Double> entry : readinessTrends.entrySet()) {
            csv.append(entry.getKey().format(DATE_FORMAT)).append(",");
            csv.append(String.format("%.1f", entry.getValue())).append("\n");
        }
        csv.append("\n");

        // Completed Activities
        if (!completedActivities.isEmpty()) {
            csv.append("COMPLETED ACTIVITIES\n");
            csv.append("Activity Details\n");
            for (String activity : completedActivities) {
                csv.append(activity).append("\n");
            }
        }

        outputStream.write(csv.toString().getBytes());
        return outputStream.toByteArray();
    }

    /**
     * Check if a report was successfully generated.
     */
    public boolean isReportGenerated(byte[] reportData) {
        return reportData != null && reportData.length > 0;
    }

    /**
     * Get the suggested filename for the report.
     */
    public String getReportFilename(String athleteName, LocalDate weekStart) {
        return String.format("weekly_report_%s_%s.csv",
                athleteName.replace(" ", "_"),
                weekStart.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    }

    /**
     * Export weekly report as JSON.
     */
    public byte[] exportWeeklyReportAsJson(
            String athleteName,
            LocalDate weekStart,
            LocalDate weekEnd,
            ComplianceSummary compliance,
            Map<LocalDate, Double> readinessTrends,
            List<String> completedActivities) throws IOException {

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"report\": {\n");
        json.append("    \"type\": \"weekly\",\n");
        json.append("    \"athlete\": \"").append(athleteName).append("\",\n");
        json.append("    \"period\": {\n");
        json.append("      \"start\": \"").append(weekStart).append("\",\n");
        json.append("      \"end\": \"").append(weekEnd).append("\"\n");
        json.append("    },\n");
        
        // Compliance section
        json.append("    \"compliance\": {\n");
        json.append("      \"completionRate\": ").append(String.format("%.2f", compliance.completionPercent())).append(",\n");
        json.append("      \"keySessionCompletion\": ").append(String.format("%.2f", compliance.keySessionCompletionPercent())).append(",\n");
        json.append("      \"zoneDistributionAdherence\": ").append(String.format("%.2f", compliance.zoneDistributionAdherencePercent())).append(",\n");
        json.append("      \"unplannedLoadMinutes\": ").append(compliance.unplannedLoadMinutes()).append(",\n");
        json.append("      \"flags\": [");
        if (!compliance.flags().isEmpty()) {
            for (int i = 0; i < compliance.flags().size(); i++) {
                json.append("\"").append(compliance.flags().get(i)).append("\"");
                if (i < compliance.flags().size() - 1) json.append(",");
            }
        }
        json.append("]\n");
        json.append("  },\n");
        
        // Readiness trends
        json.append("  \"readinessTrends\": {");
        int trendCount = 0;
        for (Map.Entry<LocalDate, Double> entry : readinessTrends.entrySet()) {
            if (trendCount > 0) json.append(",");
            json.append("\"").append(entry.getKey()).append("\": ").append(String.format("%.2f", entry.getValue()));
            trendCount++;
        }
        json.append("},\n");
        
        // Completed activities
        json.append("  \"completedActivities\": [");
        if (!completedActivities.isEmpty()) {
            for (int i = 0; i < completedActivities.size(); i++) {
                json.append("\"").append(completedActivities.get(i).replace("\"", "\\\"")).append("\"");
                if (i < completedActivities.size() - 1) json.append(",");
            }
        }
        json.append("]\n");
        
        json.append("  }\n");
        json.append("}\n");
        
        return json.toString().getBytes();
    }
}
