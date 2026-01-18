package com.training.coach.wellness.presentation;

import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.Grams;
import com.training.coach.shared.domain.unit.HeartRateVariability;
import com.training.coach.shared.domain.unit.Hours;
import com.training.coach.wellness.application.service.RecoveryRecommendationService;
import com.training.coach.wellness.application.service.WellnessReportService;
import com.training.coach.wellness.application.service.WellnessSubmissionService;
import com.training.coach.wellness.domain.model.RecoveryRecommendations;
import com.training.coach.wellness.domain.model.WellnessReport;
import com.training.coach.wellness.domain.model.WellnessSnapshot;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wellness")
public class WellnessController {

    private final WellnessSubmissionService submissionService;
    private final WellnessReportService reportService;
    private final RecoveryRecommendationService recoveryService;

    public WellnessController(
            WellnessSubmissionService submissionService,
            WellnessReportService reportService,
            RecoveryRecommendationService recoveryService) {
        this.submissionService = submissionService;
        this.reportService = reportService;
        this.recoveryService = recoveryService;
    }

    @PostMapping("/submit")
    public ResponseEntity<WellnessSnapshot> submitWellness(@RequestBody WellnessSubmissionRequest request) {
        WellnessSnapshot snapshot = submissionService.submitWellness(
                request.athleteId(), request.date(), request.toSubjectiveWellness(), request.toPhysiologicalData());
        return ResponseEntity.ok(snapshot);
    }

    @GetMapping("/athletes/{athleteId}/history")
    public ResponseEntity<List<WellnessSnapshot>> getWellnessHistory(
            @PathVariable String athleteId, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        List<WellnessSnapshot> history = submissionService.getWellnessHistory(athleteId, startDate, endDate);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/athletes/{athleteId}/latest")
    public ResponseEntity<WellnessSnapshot> getLatestWellness(@PathVariable String athleteId) {
        return submissionService
                .getLatestWellness(athleteId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/athletes/{athleteId}/date/{date}")
    public ResponseEntity<WellnessSnapshot> getWellnessByDate(
            @PathVariable String athleteId, @PathVariable LocalDate date) {
        return submissionService
                .getWellnessByDate(athleteId, date)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/reports/athletes/{athleteId}")
    public ResponseEntity<WellnessReport> getWellnessReport(
            @PathVariable String athleteId, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        WellnessReport report = reportService.generateWellnessReport(athleteId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/dashboard/athletes/{athleteId}")
    public ResponseEntity<WellnessDashboardResponse> getDashboard(
            @PathVariable String athleteId, @RequestParam(defaultValue = "7") int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        WellnessDashboardResponse dashboard = reportService.generateDashboard(athleteId, startDate, endDate);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/recommendations/athletes/{athleteId}")
    public ResponseEntity<RecoveryRecommendations> getRecoveryRecommendations(
            @PathVariable String athleteId, @RequestParam(defaultValue = "7") int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        RecoveryRecommendations recommendations =
                recoveryService.generateRecommendations(athleteId, startDate, endDate);
        return ResponseEntity.ok(recommendations);
    }

    @DeleteMapping("/athletes/{athleteId}/date/{date}")
    public ResponseEntity<Void> deleteWellnessEntry(@PathVariable String athleteId, @PathVariable LocalDate date) {
        submissionService.deleteWellnessEntry(athleteId, date);
        return ResponseEntity.noContent().build();
    }

    public record WellnessSubmissionRequest(
            String athleteId,
            LocalDate date,
            int fatigueScore,
            int stressScore,
            int sleepQualityScore,
            int motivationScore,
            int muscleSorenessScore,
            String notes,
            BeatsPerMinute restingHeartRate,
            HeartRateVariability hrv,
            Grams bodyWeightKg,
            Hours sleepHours) {

        public com.training.coach.wellness.domain.model.SubjectiveWellness toSubjectiveWellness() {
            return com.training.coach.wellness.domain.model.SubjectiveWellness.withNotes(
                    fatigueScore, stressScore, sleepQualityScore, motivationScore, muscleSorenessScore, notes);
        }

        public com.training.coach.wellness.domain.model.PhysiologicalData toPhysiologicalData() {
            return new com.training.coach.wellness.domain.model.PhysiologicalData(
                    restingHeartRate, hrv, bodyWeightKg != null ? bodyWeightKg.toKilograms() : null, null);
        }
    }

    public record WellnessDashboardResponse(
            String athleteId,
            double latestReadinessScore,
            String readinessTrend,
            double averageReadinessScore,
            double averageHrv,
            double averageRhr,
            double averageSleepHours,
            int dataCoveragePercent,
            List<String> flags,
            List<String> achievements,
            LocalDate lastUpdated) {}
}
