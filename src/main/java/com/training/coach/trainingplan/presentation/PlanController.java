package com.training.coach.trainingplan.presentation;

import com.training.coach.shared.domain.unit.Hours;
import com.training.coach.trainingplan.application.service.PlanService;
import com.training.coach.trainingplan.domain.model.PlanSummary;
import com.training.coach.trainingplan.domain.model.PlanVersion;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for plan lifecycle management.
 */
@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    public ResponseEntity<PlanSummary> createPlan(@RequestBody CreatePlanRequest request) {
        PlanService.CreatePlanCommand command = new PlanService.CreatePlanCommand(
                request.athleteId(), request.phase(), request.startDate(), request.targetWeeklyHours());
        PlanSummary plan = planService.createPlan(command);
        return ResponseEntity.ok(plan);
    }

    @GetMapping
    public ResponseEntity<List<PlanSummary>> listPlans() {
        List<PlanSummary> plans = planService.listPlans();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/{planId}/versions/{version}")
    public ResponseEntity<PlanVersion> getPlanVersion(@PathVariable String planId, @PathVariable int version) {
        PlanVersion planVersion = planService.getPlanVersion(planId, version);
        return ResponseEntity.ok(planVersion);
    }

    @PostMapping("/{planId}/publish")
    public ResponseEntity<PlanSummary> publishPlan(@PathVariable String planId) {
        PlanSummary plan = planService.publishPlan(planId);
        return ResponseEntity.ok(plan);
    }

    @PostMapping("/{planId}/revise")
    public ResponseEntity<PlanSummary> revisePlan(@PathVariable String planId) {
        PlanSummary plan = planService.revisePlan(planId);
        return ResponseEntity.ok(plan);
    }

    public record CreatePlanRequest(String athleteId, String phase, LocalDate startDate, Hours targetWeeklyHours) {}
}
