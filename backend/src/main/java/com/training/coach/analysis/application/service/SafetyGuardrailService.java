package com.training.coach.analysis.application.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

/**
 * Service for evaluating safety guardrails on training adjustments.
 * Implements comprehensive guardrail rules for athlete protection.
 *
 * Guardrail Rules:
 * - SG-FATIGUE-001: Block high-intensity when fatigue >= 8 AND soreness >= 8
 * - SG-LOAD-001: Cap weekly load progression at 15%
 * - SG-RECOVERY-001: Require minimum 2 recovery days between high-intensity
 * - SG-AI-001: Filter AI suggestions when readiness < 40
 * - SG-OVERRIDE-001: Admin override with justification and audit logging
 */
@Service
public class SafetyGuardrailService {

    // Thresholds
    private static final int HIGH_FATIGUE_THRESHOLD = 8;
    private static final int HIGH_SORENESS_THRESHOLD = 8;
    private static final double LOW_READINESS_THRESHOLD = 40.0;
    private static final double LOAD_RAMP_THRESHOLD = 15.0;
    private static final int MIN_RECOVERY_DAYS = 2;

    // In-memory audit log for guardrail decisions
    private final List<AuditEntry> auditLog = new ArrayList<>();

    /**
     * Guardrail result record.
     *
     * NOTE: Prefer using factory methods to create instances. This ensures
     * consistent result creation across the codebase and makes intent clear.
     *
     * Factory methods available:
     * - approved() / approved(String suggestion)
     * - blocked(String ruleId, String reason, String alternative)
     * - blocked(String ruleId, String reason, String alternative, List<String> notifications)
     */
    public record GuardrailResult(
            boolean blocked,
            String ruleId,
            String blockingRule,
            String safeAlternative,
            List<String> notifications
    ) {
        /** Creates an approved result with no suggestion. */
        public static GuardrailResult approved() {
            return new GuardrailResult(false, null, null, null, List.of());
        }

        /** Creates an approved result with a suggestion. */
        public static GuardrailResult approved(String suggestion) {
            return new GuardrailResult(false, null, null, suggestion, List.of());
        }

        /** Creates a blocked result with default notifications (athlete, coach). */
        public static GuardrailResult blocked(String ruleId, String reason, String alternative) {
            return new GuardrailResult(
                    true, ruleId, reason, alternative, List.of("athlete", "coach")
            );
        }

        /** Creates a blocked result with custom notifications. */
        public static GuardrailResult blocked(String ruleId, String reason, String alternative, List<String> notifications) {
            return new GuardrailResult(true, ruleId, reason, alternative, notifications);
        }
    }

    /**
     * Audit entry for guardrail decisions.
     */
    public record AuditEntry(
            String id,
            String ruleId,
            String athleteId,
            String decision,
            String reason,
            Instant timestamp,
            String performedBy,
            String justification
    ) {
        public static AuditEntry create(String ruleId, String athleteId, String decision, String reason, String performedBy) {
            return new AuditEntry(
                    "AUDIT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    ruleId, athleteId, decision, reason, Instant.now(), performedBy, null
            );
        }

        public static AuditEntry override(String ruleId, String athleteId, String reason, String adminUser, String justification) {
            return new AuditEntry(
                    "AUDIT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    ruleId, athleteId, "OVERRIDE", reason, Instant.now(), adminUser, justification
            );
        }
    }

    /**
     * Check if an adjustment is blocked by safety guardrails.
     * Evaluates all applicable rules in priority order.
     */
    public GuardrailResult checkAdjustment(
            String athleteId,
            double fatigueScore,
            double sorenessScore,
            double readinessScore,
            String workoutType) {

        return checkAdjustment(athleteId, fatigueScore, sorenessScore, readinessScore, workoutType, null);
    }

    /**
     * Check if an adjustment is blocked by safety guardrails with admin override.
     */
    public GuardrailResult checkAdjustment(
            String athleteId,
            double fatigueScore,
            double sorenessScore,
            double readinessScore,
            String workoutType,
            String overrideUser) {

        // SG-FATIGUE-001: Block high-intensity when fatigue >= 8 AND soreness >= 8
        if (isHighIntensityWorkout(workoutType)) {
            if (fatigueScore >= HIGH_FATIGUE_THRESHOLD && sorenessScore >= HIGH_SORENESS_THRESHOLD) {
                logAudit(GuardrailResult.blocked(
                        "SG-FATIGUE-001",
                        "High fatigue (" + fatigueScore + ") and soreness (" + sorenessScore + ") detected. High-intensity workouts are blocked.",
                        "Schedule a recovery ride (Zone 1), active recovery, or rest day"
                ), athleteId, overrideUser);
                return GuardrailResult.blocked(
                        "SG-FATIGUE-001",
                        "High fatigue (" + fatigueScore + ") and soreness (" + sorenessScore + ") detected. High-intensity workouts are blocked.",
                        "Schedule a recovery ride (Zone 1), active recovery, or rest day"
                );
            }
        }

        // SG-LOAD-001: Check load ramp (placeholder - would need weeklyLoad and proposedWeeklyLoad)
        // This would be checked with additional parameters in a full implementation

        // SG-RECOVERY-001: Check recovery days (placeholder - would need lastHighIntensityDate)
        // This would be checked with additional parameters in a full implementation

        // If no rules block, return approved
        logAudit(GuardrailResult.approved("Consider maintaining current training load"), athleteId, overrideUser);
        return GuardrailResult.approved("Consider maintaining current training load");
    }

    /**
     * SG-LOAD-001: Check if weekly load ramp exceeds threshold.
     */
    public GuardrailResult checkLoadRamp(
            String athleteId,
            double currentWeeklyLoad,
            double proposedWeeklyLoad,
            String overrideUser) {

        if (currentWeeklyLoad > 0) {
            double rampPercent = ((proposedWeeklyLoad - currentWeeklyLoad) / currentWeeklyLoad) * 100.0;
            if (rampPercent > LOAD_RAMP_THRESHOLD) {
                double maxSafeLoad = currentWeeklyLoad * (1.0 + LOAD_RAMP_THRESHOLD / 100.0);
                String rationale = String.format(
                        "Load increase of %.1f%% exceeds the %%%.1f ramp cap to prevent non-functional overreaching. " +
                        "Gradual progression allows physiological adaptation while minimizing injury risk.",
                        rampPercent, LOAD_RAMP_THRESHOLD
                );

                logAudit(GuardrailResult.blocked(
                        "SG-LOAD-001",
                        rationale,
                        String.format("Maximum safe load: %.0f TSS", maxSafeLoad)
                ), athleteId, overrideUser);

                return GuardrailResult.blocked(
                        "SG-LOAD-001",
                        rationale,
                        String.format("Maximum safe load: %.0f TSS", maxSafeLoad)
                );
            }
        }

        return GuardrailResult.approved();
    }

    /**
     * SG-RECOVERY-001: Check minimum recovery days between high-intensity sessions.
     */
    public GuardrailResult checkRecoveryDays(
            String athleteId,
            LocalDate lastHighIntensityDate,
            String workoutType,
            String overrideUser) {

        if (isHighIntensityWorkout(workoutType) && lastHighIntensityDate != null) {
            long daysSince = java.time.temporal.ChronoUnit.DAYS.between(lastHighIntensityDate, LocalDate.now());
            if (daysSince < MIN_RECOVERY_DAYS) {
                logAudit(GuardrailResult.blocked(
                        "SG-RECOVERY-001",
                        "Only " + daysSince + " day(s) since last high-intensity session. Minimum " + MIN_RECOVERY_DAYS + " recovery days required for physiological adaptation.",
                        "Schedule active recovery (Zone 1), rest day, or endurance ride (Zone 2)"
                ), athleteId, overrideUser);

                return GuardrailResult.blocked(
                        "SG-RECOVERY-001",
                        "Only " + daysSince + " day(s) since last high-intensity session. Minimum " + MIN_RECOVERY_DAYS + " recovery days required for physiological adaptation.",
                        "Schedule active recovery (Zone 1), rest day, or endurance ride (Zone 2)"
                );
            }
        }

        return GuardrailResult.approved();
    }

    /**
     * SG-AI-001: Filter AI suggestions based on readiness.
     */
    public List<String> filterAiSuggestions(String athleteId, double readinessScore, List<String> suggestions) {
        if (readinessScore >= LOW_READINESS_THRESHOLD) {
            return suggestions; // No filtering needed if readiness is okay
        }

        List<String> filtered = new ArrayList<>();
        List<String> rejected = new ArrayList<>();

        for (String suggestion : suggestions) {
            if (isUnsafeAiSuggestion(suggestion)) {
                rejected.add(suggestion);
            } else {
                filtered.add(suggestion);
            }
        }

        // Log the filtering decision
        logAudit(new GuardrailResult(
                false, "SG-AI-001",
                "AI suggestions filtered: " + rejected.size() + " rejected out of " + suggestions.size(),
                "Filtered suggestions ready", List.of()
        ), athleteId, null);

        return filtered;
    }

    /**
     * SG-OVERRIDE-001: Attempt to override a blocked guardrail decision.
     */
    public GuardrailResult attemptOverride(
            GuardrailResult currentResult,
            String athleteId,
            String adminUser,
            String justification) {

        if (!currentResult.blocked()) {
            return currentResult; // Nothing to override
        }

        // Log the override
        AuditEntry overrideEntry = AuditEntry.override(
                currentResult.ruleId(),
                athleteId,
                currentResult.blockingRule(),
                adminUser,
                justification
        );
        auditLog.add(overrideEntry);

        return new GuardrailResult(
                false,
                currentResult.ruleId(),
                "GUARDRAIL OVERRIDE: " + currentResult.ruleId(),
                "Justification: " + justification + " | Approved by: " + adminUser,
                List.of("coach", "admin")
        );
    }

    /**
     * Get all audit entries for an athlete.
     */
    public List<AuditEntry> getAuditEntries(String athleteId) {
        return auditLog.stream()
                .filter(e -> e.athleteId().equals(athleteId))
                .toList();
    }

    /**
     * Clear audit log (for testing).
     */
    public void clearAuditLog() {
        auditLog.clear();
    }

    private boolean isHighIntensityWorkout(String workoutType) {
        if (workoutType == null) return false;
        return switch (workoutType.toUpperCase()) {
            case "INTERVALS", "VO2_MAX", "THRESHOLD", "SPRINT" -> true;
            default -> false;
        };
    }

    private boolean isUnsafeAiSuggestion(String suggestion) {
        String lower = suggestion.toLowerCase();
        return lower.contains("increase interval intensity") ||
                lower.contains("extra vo2 max") ||
                lower.contains("add extra sprint") ||
                (lower.contains("hard") && lower.contains("workout"));
    }

    private void logAudit(GuardrailResult result, String athleteId, String overrideUser) {
        if (result.blocked() || result.ruleId() != null) {
            AuditEntry entry = AuditEntry.create(
                    result.ruleId(),
                    athleteId,
                    overrideUser != null ? "OVERRIDE" : "BLOCKED",
                    result.blockingRule(),
                    overrideUser != null ? overrideUser : "system"
            );
            auditLog.add(entry);
        }
    }
}
