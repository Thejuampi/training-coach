package com.training.coach.analysis.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a proposal to adjust a training plan.
 * Proposals go through a lifecycle before being applied or rejected.
 */
public record AdjustmentProposal(
        String id,
        String planId,
        String athleteId,
        AdjustmentType type,
        String description,
        Map<String, Object> parameters,
        ProposalStatus status,
        Instant proposedAt,
        String proposedBy,
        Instant reviewedAt,
        String reviewedBy,
        String rejectionReason,
        GuardrailCheckResult guardrailResult
) {
    public AdjustmentProposal {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Proposal ID cannot be null or blank");
        }
        if (planId == null || planId.isBlank()) {
            throw new IllegalArgumentException("Plan ID cannot be null or blank");
        }
        if (athleteId == null || athleteId.isBlank()) {
            throw new IllegalArgumentException("Athlete ID cannot be null or blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("Adjustment type cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Proposal status cannot be null");
        }
        if (proposedAt == null) {
            throw new IllegalArgumentException("Proposed at timestamp cannot be null");
        }
    }

    /**
     * Create a new adjustment proposal.
     */
    public static AdjustmentProposal create(
            String planId,
            String athleteId,
            AdjustmentType type,
            String description,
            Map<String, Object> parameters,
            String proposedBy
    ) {
        return new AdjustmentProposal(
                UUID.randomUUID().toString(),
                planId,
                athleteId,
                type,
                description,
                parameters,
                ProposalStatus.PENDING,
                Instant.now(),
                proposedBy,
                null,
                null,
                null,
                null
        );
    }

    /**
     * Approve the proposal.
     */
    public AdjustmentProposal approve(String reviewedBy) {
        return new AdjustmentProposal(
                id,
                planId,
                athleteId,
                type,
                description,
                parameters,
                ProposalStatus.APPROVED,
                proposedAt,
                proposedBy,
                Instant.now(),
                reviewedBy,
                null,
                guardrailResult
        );
    }

    /**
     * Reject the proposal with a reason.
     */
    public AdjustmentProposal reject(String reviewedBy, String reason) {
        return new AdjustmentProposal(
                id,
                planId,
                athleteId,
                type,
                description,
                parameters,
                ProposalStatus.REJECTED,
                proposedAt,
                proposedBy,
                Instant.now(),
                reviewedBy,
                reason,
                guardrailResult
        );
    }

    /**
     * Apply the proposal (changes status to APPLIED).
     */
    public AdjustmentProposal markAsApplied() {
        return new AdjustmentProposal(
                id,
                planId,
                athleteId,
                type,
                description,
                parameters,
                ProposalStatus.APPLIED,
                proposedAt,
                proposedBy,
                reviewedAt,
                reviewedBy,
                rejectionReason,
                guardrailResult
        );
    }

    /**
     * Check if the proposal is pending review.
     */
    public boolean isPending() {
        return status == ProposalStatus.PENDING;
    }

    /**
     * Check if the proposal was approved.
     */
    public boolean isApproved() {
        return status == ProposalStatus.APPROVED;
    }

    /**
     * Check if the proposal was rejected.
     */
    public boolean isRejected() {
        return status == ProposalStatus.REJECTED;
    }

    /**
     * Types of adjustments that can be proposed.
     */
    public enum AdjustmentType {
        REDUCE_INTENSITY,           // Reduce workout intensity by specified amount
        REDUCE_VOLUME,              // Reduce training volume by specified percentage
        INCREASE_VOLUME,            // Increase training volume by specified percentage
        SWAP_WORKOUT,               // Replace one workout type with another
        ADD_REST_DAY,               // Insert an additional rest day
        RESCHEDULE_WORKOUT,         // Move a workout to a different date
        MODIFY_TAPER,               // Adjust taper duration
        CUSTOM                      // Custom adjustment not covered by other types
    }

    /**
     * Status of a proposal through its lifecycle.
     */
    public enum ProposalStatus {
        PENDING,     // Awaiting review
        APPROVED,    // Approved and ready to apply
        REJECTED,    // Rejected by reviewer
        APPLIED,     // Successfully applied to plan
        EXPIRED      // Proposal expired without action
    }

    /**
     * Result of checking the proposal against safety guardrails.
     */
    public record GuardrailCheckResult(
            boolean passed,
            boolean blocked,
            String blockingRule,
            String warningMessage
    ) {
        public static GuardrailCheckResult createPassed() {
            return new GuardrailCheckResult(true, false, null, null);
        }

        public static GuardrailCheckResult blocked(String rule, String reason) {
            return new GuardrailCheckResult(false, true, rule, reason);
        }

        public static GuardrailCheckResult warning(String message) {
            return new GuardrailCheckResult(true, false, null, message);
        }
    }
}