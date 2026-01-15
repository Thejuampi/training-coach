package com.training.coach.analysis.presentation;

import com.training.coach.analysis.application.service.AdjustmentService;
import com.training.coach.analysis.application.service.ComplianceService;
import com.training.coach.analysis.application.service.ReadinessService;
import com.training.coach.analysis.application.service.TrendService;
import com.training.coach.athlete.domain.model.ReadinessSnapshot;
import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.HeartRateVariability;
import com.training.coach.shared.domain.unit.Hours;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final ReadinessService readinessService;
    private final ComplianceService complianceService;
    private final AdjustmentService adjustmentService;
    private final TrendService trendService;

    public AnalysisController(
            ReadinessService readinessService,
            ComplianceService complianceService,
            AdjustmentService adjustmentService,
            TrendService trendService) {
        this.readinessService = readinessService;
        this.complianceService = complianceService;
        this.adjustmentService = adjustmentService;
        this.trendService = trendService;
    }

    @PostMapping("/readiness")
    public ResponseEntity<ReadinessSnapshot> calculateReadiness(@RequestBody ReadinessRequest request) {
        ReadinessSnapshot snapshot = readinessService.calculateReadiness(
                request.rhr(), request.hrv(), request.sleepHours(), request.sleepQuality());
        return ResponseEntity.ok(snapshot);
    }

    @PostMapping("/compliance")
    public ResponseEntity<Double> calculateCompliance(@RequestBody ComplianceRequest request) {
        double compliance =
                complianceService.calculateCompliance(request.plannedWorkouts(), request.completedWorkouts());
        return ResponseEntity.ok(compliance);
    }

    @PostMapping("/adjustments")
    public ResponseEntity<String> suggestAdjustment(@RequestBody AdjustmentRequest request) {
        String suggestion = adjustmentService.suggestAdjustment(request.readinessScore(), request.compliance());
        return ResponseEntity.ok(suggestion);
    }

    @PostMapping("/trends")
    public ResponseEntity<String> calculateTrend(@RequestBody TrendRequest request) {
        String trend = trendService.calculateTrend(request.values());
        return ResponseEntity.ok(trend);
    }

    public record ReadinessRequest(BeatsPerMinute rhr, HeartRateVariability hrv, Hours sleepHours, int sleepQuality) {}

    public record ComplianceRequest(int plannedWorkouts, int completedWorkouts) {}

    public record AdjustmentRequest(double readinessScore, double compliance) {}

    public record TrendRequest(List<Double> values) {}
}
