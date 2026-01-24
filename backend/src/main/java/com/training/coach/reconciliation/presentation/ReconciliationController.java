package com.training.coach.reconciliation.presentation;

import com.training.coach.reconciliation.application.service.ReconciliationService;
import com.training.coach.reconciliation.domain.model.DataConflict;
import com.training.coach.reconciliation.domain.model.PrecedenceRule;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST controller for multi-platform reconciliation operations.
 */
@RestController
@RequestMapping("/api/reconciliation")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    /**
     * Detect duplicate activities.
     */
    @PostMapping("/detect/duplicates")
    public ResponseEntity<DataConflict> detectDuplicates(
            @RequestParam String athleteId,
            @RequestParam LocalDateTime activityDate,
            @RequestBody Map<String, DataConflict.ConflictRecord> conflictingRecords
    ) {
        DataConflict conflict = reconciliationService.detectDuplicates(
                athleteId,
                activityDate,
                conflictingRecords
        );
        return ResponseEntity.ok(conflict);
    }

    /**
     * Detect overlapping activities.
     */
    @PostMapping("/detect/overlaps")
    public ResponseEntity<DataConflict> detectOverlaps(
            @RequestParam String athleteId,
            @RequestParam LocalDateTime activityDate,
            @RequestBody Map<String, DataConflict.ConflictRecord> conflictingRecords
    ) {
        DataConflict conflict = reconciliationService.detectOverlaps(
                athleteId,
                activityDate,
                conflictingRecords
        );
        return ResponseEntity.ok(conflict);
    }

    /**
     * Run full reconciliation for an athlete.
     */
    @PostMapping("/run/{athleteId}")
    public ResponseEntity<ReconciliationService.ReconciliationResult> runReconciliation(
            @PathVariable String athleteId,
            @RequestBody List<Map<String, Object>> activities
    ) {
        ReconciliationService.ReconciliationResult result =
                reconciliationService.runReconciliation(athleteId, activities);
        return ResponseEntity.ok(result);
    }

    /**
     * Manually resolve a conflict.
     */
    @PostMapping("/conflicts/{conflictId}/resolve")
    public ResponseEntity<DataConflict> resolveConflict(
            @PathVariable String conflictId,
            @RequestParam String primaryPlatform,
            @RequestBody List<String> retainedSources,
            @RequestParam String resolution
    ) {
        DataConflict resolved = reconciliationService.resolveConflict(
                conflictId,
                primaryPlatform,
                retainedSources,
                resolution
        );
        return ResponseEntity.ok(resolved);
    }

    /**
     * Set precedence rule for an athlete.
     */
    @PostMapping("/rules/precedence")
    public ResponseEntity<PrecedenceRule> setPrecedenceRule(
            @RequestParam String athleteId,
            @RequestParam String ruleName,
            @RequestBody Map<String, Integer> platformPrecedence
    ) {
        PrecedenceRule rule = reconciliationService.setPrecedenceRule(
                athleteId,
                ruleName,
                platformPrecedence
        );
        return ResponseEntity.ok(rule);
    }

    /**
     * Get the active precedence rule for an athlete.
     */
    @GetMapping("/rules/precedence/{athleteId}")
    public ResponseEntity<PrecedenceRule> getPrecedenceRule(@PathVariable String athleteId) {
        PrecedenceRule rule = reconciliationService.getPrecedenceRule(athleteId);
        return ResponseEntity.ok(rule);
    }

    /**
     * Get unresolved conflicts for an athlete.
     */
    @GetMapping("/conflicts/unresolved/{athleteId}")
    public ResponseEntity<List<DataConflict>> getUnresolvedConflicts(@PathVariable String athleteId) {
        List<DataConflict> conflicts = reconciliationService.getUnresolvedConflicts(athleteId);
        return ResponseEntity.ok(conflicts);
    }

    /**
     * Get all conflicts requiring manual review.
     */
    @GetMapping("/conflicts/requires-review")
    public ResponseEntity<List<DataConflict>> getConflictsRequiringReview() {
        List<DataConflict> conflicts = reconciliationService.getConflictsRequiringReview();
        return ResponseEntity.ok(conflicts);
    }
}
