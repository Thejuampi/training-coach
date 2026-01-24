package com.training.coach.analysis.presentation;

import com.training.coach.analysis.application.service.AIAdjustmentService;
import com.training.coach.analysis.application.service.AdjustmentService;
import com.training.coach.analysis.application.port.out.AdjustmentProposalRepository;
import com.training.coach.analysis.domain.model.AdjustmentProposal;
import com.training.coach.athlete.domain.model.TrainingPlan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for plan adjustment operations.
 */
@RestController
@RequestMapping("/api/adjustments")
public class AdjustmentController {

    private final AdjustmentService adjustmentService;
    private final AIAdjustmentService aiAdjustmentService;
    private final AdjustmentProposalRepository proposalRepository;

    public AdjustmentController(
            AdjustmentService adjustmentService,
            AIAdjustmentService aiAdjustmentService,
            AdjustmentProposalRepository proposalRepository) {
        this.adjustmentService = adjustmentService;
        this.aiAdjustmentService = aiAdjustmentService;
        this.proposalRepository = proposalRepository;
    }

    /**
     * Generate adjustment suggestions for an athlete's plan.
     */
    @PostMapping("/proposals/generate/{athleteId}/{planId}")
    public ResponseEntity<List<AdjustmentProposal>> generateSuggestions(
            @PathVariable String athleteId,
            @PathVariable String planId
    ) {
        List<AdjustmentProposal> suggestions = aiAdjustmentService.generateSuggestions(athleteId, planId);
        // Save all suggestions
        suggestions.forEach(proposalRepository::save);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * Propose a manual adjustment.
     */
    @PostMapping("/proposals")
    public ResponseEntity<AdjustmentProposal> proposeAdjustment(
            @RequestParam String planId,
            @RequestParam String athleteId,
            @RequestParam AdjustmentProposal.AdjustmentType type,
            @RequestParam String description,
            @RequestBody Map<String, Object> parameters,
            @RequestParam String proposedBy
    ) {
        AdjustmentProposal proposal = AdjustmentProposal.create(
                planId,
                athleteId,
                type,
                description,
                parameters,
                proposedBy
        );
        AdjustmentProposal saved = proposalRepository.save(proposal);
        return ResponseEntity.ok(saved);
    }

    /**
     * Approve an adjustment proposal.
     */
    @PostMapping("/proposals/{proposalId}/approve")
    public ResponseEntity<AdjustmentProposal> approveProposal(
            @PathVariable String proposalId,
            @RequestParam String reviewedBy
    ) {
        AdjustmentProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));

        // Check guardrails
        if (proposal.guardrailResult() != null && proposal.guardrailResult().blocked()) {
            return ResponseEntity.badRequest().build();
        }

        AdjustmentProposal approved = proposal.approve(reviewedBy);
        proposalRepository.save(approved);

        // Apply the adjustment
        TrainingPlan adjustedPlan = adjustmentService.approveAdjustment(
                proposal.planId(),
                proposal.athleteId(),
                proposal.type().name()
        );

        // Mark as applied
        AdjustmentProposal applied = approved.markAsApplied();
        proposalRepository.save(applied);

        return ResponseEntity.ok(applied);
    }

    /**
     * Reject an adjustment proposal.
     */
    @PostMapping("/proposals/{proposalId}/reject")
    public ResponseEntity<AdjustmentProposal> rejectProposal(
            @PathVariable String proposalId,
            @RequestParam String reviewedBy,
            @RequestParam String reason
    ) {
        AdjustmentProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));

        AdjustmentProposal rejected = proposal.reject(reviewedBy, reason);
        AdjustmentProposal saved = proposalRepository.save(rejected);
        return ResponseEntity.ok(saved);
    }

    /**
     * Get pending proposals for a plan.
     */
    @GetMapping("/proposals/pending/{planId}")
    public ResponseEntity<List<AdjustmentProposal>> getPendingProposals(@PathVariable String planId) {
        List<AdjustmentProposal> pending = proposalRepository.findPendingByPlanId(planId);
        return ResponseEntity.ok(pending);
    }

    /**
     * Get audit log for an athlete.
     */
    @GetMapping("/audit/{athleteId}")
    public ResponseEntity<List<AdjustmentService.AdjustmentAuditEntry>> getAuditLog(
            @PathVariable String athleteId
    ) {
        List<AdjustmentService.AdjustmentAuditEntry> auditLog =
                adjustmentService.getAdjustmentAuditLog(athleteId);
        return ResponseEntity.ok(auditLog);
    }

    /**
     * Get adjustment suggestion based on readiness and compliance.
     */
    @GetMapping("/suggest")
    public ResponseEntity<String> getSuggestion(
            @RequestParam double readinessScore,
            @RequestParam double compliance
    ) {
        String suggestion = adjustmentService.suggestAdjustment(readinessScore, compliance);
        return ResponseEntity.ok(suggestion);
    }
}