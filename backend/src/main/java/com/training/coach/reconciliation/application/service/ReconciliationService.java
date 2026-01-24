package com.training.coach.reconciliation.application.service;

import com.training.coach.reconciliation.application.port.out.DataConflictRepository;
import com.training.coach.reconciliation.application.port.out.PrecedenceRuleRepository;
import com.training.coach.reconciliation.domain.model.DataConflict;
import com.training.coach.reconciliation.domain.model.PrecedenceRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for detecting and reconciling conflicts between multiple fitness platforms.
 */
@Service
public class ReconciliationService {

    private static final Logger logger = LoggerFactory.getLogger(ReconciliationService.class);

    private final DataConflictRepository conflictRepository;
    private final PrecedenceRuleRepository ruleRepository;

    public ReconciliationService(
            DataConflictRepository conflictRepository,
            PrecedenceRuleRepository ruleRepository) {
        this.conflictRepository = conflictRepository;
        this.ruleRepository = ruleRepository;
    }

    /**
     * Detect duplicate activities across platforms.
     */
    public DataConflict detectDuplicates(
            String athleteId,
            LocalDateTime activityDate,
            Map<String, DataConflict.ConflictRecord> conflictingRecords
    ) {
        if (conflictingRecords.size() < 2) {
            throw new IllegalArgumentException("At least 2 conflicting records required");
        }

        DataConflict conflict = DataConflict.create(
                athleteId,
                activityDate,
                DataConflict.ConflictType.DUPLICATE,
                conflictingRecords
        );

        // Check if we have a precedence rule
        ruleRepository.findActiveByAthleteId(athleteId).ifPresent(rule -> {
            String primaryPlatform = rule.getPrimaryPlatform();
            DataConflict resolved = autoResolve(conflict, rule, primaryPlatform);
            conflictRepository.save(resolved);
        });

        DataConflict saved = conflictRepository.save(conflict);
        logger.info("Detected duplicate activity for athlete {} on {}", athleteId, activityDate);
        return saved;
    }

    /**
     * Detect overlapping activities.
     */
    public DataConflict detectOverlaps(
            String athleteId,
            LocalDateTime activityDate,
            Map<String, DataConflict.ConflictRecord> conflictingRecords
    ) {
        DataConflict conflict = DataConflict.create(
                athleteId,
                activityDate,
                DataConflict.ConflictType.OVERLAPPING,
                conflictingRecords
        );

        // Overlaps typically require manual review
        DataConflict requiresReview = conflict.requireManualReview();
        DataConflict saved = conflictRepository.save(requiresReview);
        logger.info("Detected overlapping activities for athlete {} on {}", athleteId, activityDate);
        return saved;
    }

    /**
     * Detect conflicting data values for the same activity.
     */
    public DataConflict detectConflictingData(
            String athleteId,
            LocalDateTime activityDate,
            Map<String, DataConflict.ConflictRecord> conflictingRecords
    ) {
        DataConflict conflict = DataConflict.create(
                athleteId,
                activityDate,
                DataConflict.ConflictType.DISCREPANT_DATA,
                conflictingRecords
        );

        DataConflict saved = conflictRepository.save(conflict);
        logger.info("Detected conflicting data for athlete {} on {}", athleteId, activityDate);
        return saved;
    }

    /**
     * Auto-resolve a conflict using precedence rules.
     */
    public DataConflict autoResolve(
            DataConflict conflict,
            PrecedenceRule rule,
            String primaryPlatform
    ) {
        // If we have a clear primary platform, auto-resolve
        if (primaryPlatform != null && conflict.conflictingRecords().containsKey(primaryPlatform)) {
            List<String> retainedSources = List.of(primaryPlatform);
            String resolution = String.format(
                    "Auto-resolved using precedence rule '%s'. Platform '%s' retained as source of truth.",
                    rule.ruleName(),
                    primaryPlatform
            );

            return conflict.resolve(resolution, primaryPlatform, retainedSources);
        }

        // Otherwise, require manual review
        return conflict.requireManualReview();
    }

    /**
     * Manually resolve a conflict.
     */
    public DataConflict resolveConflict(
            String conflictId,
            String primaryPlatform,
            List<String> retainedSources,
            String resolution
    ) {
        DataConflict conflict = conflictRepository.findById(conflictId)
                .orElseThrow(() -> new IllegalArgumentException("Conflict not found: " + conflictId));

        DataConflict resolved = conflict.resolve(resolution, primaryPlatform, retainedSources);
        DataConflict saved = conflictRepository.save(resolved);
        logger.info("Manually resolved conflict {} with platform {} as source", conflictId, primaryPlatform);
        return saved;
    }

    /**
     * Create or update a precedence rule for an athlete.
     */
    public PrecedenceRule setPrecedenceRule(
            String athleteId,
            String ruleName,
            Map<String, Integer> platformPrecedence
    ) {
        // Deactivate existing rule if present
        ruleRepository.findActiveByAthleteId(athleteId).ifPresent(rule -> {
            PrecedenceRule deactivated = rule.deactivate();
            ruleRepository.save(deactivated);
        });

        PrecedenceRule rule = PrecedenceRule.create(athleteId, ruleName, platformPrecedence);
        PrecedenceRule saved = ruleRepository.save(rule);
        logger.info("Set precedence rule '{}' for athlete {}", ruleName, athleteId);
        return saved;
    }

    /**
     * Get the active precedence rule for an athlete.
     */
    public PrecedenceRule getPrecedenceRule(String athleteId) {
        return ruleRepository.findActiveByAthleteId(athleteId)
                .orElseThrow(() -> new IllegalArgumentException("No precedence rule found for athlete: " + athleteId));
    }

    /**
     * Get all unresolved conflicts for an athlete.
     */
    public List<DataConflict> getUnresolvedConflicts(String athleteId) {
        return conflictRepository.findUnresolvedByAthleteId(athleteId);
    }

    /**
     * Get conflicts requiring manual review.
     */
    public List<DataConflict> getConflictsRequiringReview() {
        return conflictRepository.findRequiresReview();
    }

    /**
     * Run reconciliation for an athlete's activities.
     */
    public ReconciliationResult runReconciliation(String athleteId, List<Map<String, Object>> activities) {
        // Group activities by date/time
        Map<LocalDateTime, Map<String, DataConflict.ConflictRecord>> groupedByDate = new HashMap<>();

        for (Map<String, Object> activity : activities) {
            String platform = (String) activity.get("platform");
            String activityId = (String) activity.get("activityId");
            LocalDateTime startTime = (LocalDateTime) activity.get("startTime");
            double duration = ((Number) activity.getOrDefault("duration", 0)).doubleValue();

            DataConflict.ConflictRecord record = new DataConflict.ConflictRecord(
                    platform,
                    activityId,
                    duration,
                    startTime,
                    activity
            );

            groupedByDate.computeIfAbsent(startTime, k -> new HashMap<>()).put(platform, record);
        }

        // Detect conflicts
        int duplicatesDetected = 0;
        int overlapsDetected = 0;
        int autoResolved = 0;
        int requiresReview = 0;

        for (Map.Entry<LocalDateTime, Map<String, DataConflict.ConflictRecord>> entry : groupedByDate.entrySet()) {
            if (entry.getValue().size() > 1) {
                DataConflict conflict = detectDuplicates(
                        athleteId,
                        entry.getKey(),
                        entry.getValue()
                );

                if (conflict.isResolved()) {
                    autoResolved++;
                } else if (conflict.requiresManualReview()) {
                    requiresReview++;
                }

                duplicatesDetected++;
            }
        }

        return new ReconciliationResult(
                duplicatesDetected,
                overlapsDetected,
                autoResolved,
                requiresReview,
                LocalDateTime.now()
        );
    }

    /**
     * Result of a reconciliation operation.
     */
    public record ReconciliationResult(
            int duplicatesDetected,
            int overlapsDetected,
            int autoResolved,
            int requiresReview,
            LocalDateTime processedAt
    ) {}
}
