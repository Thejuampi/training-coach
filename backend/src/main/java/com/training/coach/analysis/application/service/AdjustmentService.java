package com.training.coach.analysis.application.service;

import com.training.coach.analysis.application.service.SafetyGuardrailService.GuardrailResult;
import com.training.coach.athlete.application.port.out.AthleteRepository;
import com.training.coach.athlete.domain.model.Athlete;
import com.training.coach.athlete.domain.model.TrainingPlan;
import com.training.coach.trainingplan.application.service.TrainingPlanService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service for suggesting and managing plan adjustments based on readiness and compliance.
 */
@Service
public class AdjustmentService {

    private final SafetyGuardrailService safetyGuardrailService;
    private final TrainingPlanService trainingPlanService;
    private final AthleteRepository athleteRepository;
    
    // In-memory audit log for adjustments
    private final List<AdjustmentAuditEntry> adjustmentAuditLog = new ArrayList<>();

    public AdjustmentService(SafetyGuardrailService safetyGuardrailService, TrainingPlanService trainingPlanService, AthleteRepository athleteRepository) {
        this.safetyGuardrailService = safetyGuardrailService;
        this.trainingPlanService = trainingPlanService;
        this.athleteRepository = athleteRepository;
    }

    public String suggestAdjustment(double readinessScore, double compliance) {
        if (readinessScore < 5.0) {
            return "Reduce volume by 20-30% and focus on recovery. Consider swapping high-intensity sessions.";
        } else if (compliance < 80.0) {
            return "Increase motivation cues or adjust schedule. Review workout accessibility.";
        } else {
            return "Maintain current plan. Athlete is performing well.";
        }
    }

    /**
     * Propose a plan adjustment and check against safety guardrails.
     */
    public GuardrailResult proposeAdjustment(
            String athleteId,
            String adjustmentType,
            double intensityChange,
            double currentWeeklyLoad,
            double proposedWeeklyLoad,
            double fatigueScore,
            double sorenessScore,
            double readinessScore) {
        
        String workoutType = determineWorkoutType(adjustmentType);
        
        // Check guardrails
        GuardrailResult result = safetyGuardrailService.checkAdjustment(
            athleteId, fatigueScore, sorenessScore, readinessScore, workoutType, null
        );
        
        // Also check load ramp if proposed load is higher
        if (proposedWeeklyLoad > currentWeeklyLoad) {
            GuardrailResult loadResult = safetyGuardrailService.checkLoadRamp(
                athleteId, currentWeeklyLoad, proposedWeeklyLoad, null
            );
            if (loadResult.blocked()) {
                result = loadResult;
            }
        }
        
        // Log the proposal
        AdjustmentAuditEntry entry = new AdjustmentAuditEntry(
            "ADJ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
            athleteId,
            "PROPOSED",
            adjustmentType,
            intensityChange,
            result.blocked() ? "BLOCKED" : "APPROVED",
            result.blockingRule(),
            Instant.now(),
            "coach"
        );
        adjustmentAuditLog.add(entry);
        
        return result;
    }

    /**
     * Approve a proposed adjustment and apply it to the plan.
     */
    public TrainingPlan approveAdjustment(String planId, String athleteId, String adjustmentType) {
        // Find the proposal in audit log
        AdjustmentAuditEntry proposal = adjustmentAuditLog.stream()
            .filter(e -> e.athleteId().equals(athleteId) && e.adjustmentType().equals(adjustmentType))
            .reduce((a, b) -> b) // Get the most recent
            .orElse(null);
        
        if (proposal != null) {
            // Update audit entry to approved
            AdjustmentAuditEntry approved = new AdjustmentAuditEntry(
                proposal.id(),
                proposal.athleteId(),
                "APPROVED",
                proposal.adjustmentType(),
                proposal.intensityChange(),
                "APPLIED",
                null,
                Instant.now(),
                "coach"
            );
            adjustmentAuditLog.remove(proposal);
            adjustmentAuditLog.add(approved);
        }
        
        // Retrieve the athlete and apply adjustment
        Optional<Athlete> athleteOpt = athleteRepository.findById(athleteId);
        if (athleteOpt.isPresent()) {
            Athlete athlete = athleteOpt.get();
            // Apply the adjustment - generate a new plan with reduced intensity
            TrainingPlan plan = trainingPlanService.generatePlan(
                athlete,
                "adjusted",
                LocalDate.now(),
                athlete.preferences().targetWeeklyVolumeHours()
            );
            return plan;
        }
        
        // Return a minimal plan if athlete not found (shouldn't happen in normal flow)
        return null;
    }

    /**
     * Get audit log entries for an athlete's plan adjustments.
     */
    public List<AdjustmentAuditEntry> getAdjustmentAuditLog(String athleteId) {
        return adjustmentAuditLog.stream()
            .filter(e -> e.athleteId().equals(athleteId))
            .toList();
    }

    private String determineWorkoutType(String adjustmentType) {
        String lower = adjustmentType.toLowerCase();
        if (lower.contains("interval") || lower.contains("vo2") || lower.contains("threshold")) {
            return "INTERVALS";
        } else if (lower.contains("recovery") || lower.contains("easy")) {
            return "RECOVERY";
        }
        return "ENDURANCE";
    }

    /**
     * Audit entry for plan adjustments.
     */
    public record AdjustmentAuditEntry(
        String id,
        String athleteId,
        String status,
        String adjustmentType,
        double intensityChange,
        String decision,
        String blockingReason,
        Instant timestamp,
        String performedBy
    ) {}
}
