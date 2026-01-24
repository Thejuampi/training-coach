package com.training.coach.testconfig.inmemory;

import com.training.coach.analysis.application.port.out.AdjustmentProposalRepository;
import com.training.coach.analysis.domain.model.AdjustmentProposal;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory AdjustmentProposalRepository for fast tests.
 */
public class InMemoryAdjustmentProposalRepository implements AdjustmentProposalRepository {
    private final ConcurrentHashMap<String, AdjustmentProposal> proposals = new ConcurrentHashMap<>();

    @Override
    public AdjustmentProposal save(AdjustmentProposal proposal) {
        proposals.put(proposal.id(), proposal);
        return proposal;
    }

    @Override
    public Optional<AdjustmentProposal> findById(String proposalId) {
        return Optional.ofNullable(proposals.get(proposalId));
    }

    @Override
    public List<AdjustmentProposal> findByPlanId(String planId) {
        return proposals.values().stream()
                .filter(p -> p.planId().equals(planId))
                .collect(Collectors.toList());
    }

    @Override
    public List<AdjustmentProposal> findByAthleteId(String athleteId) {
        return proposals.values().stream()
                .filter(p -> p.athleteId().equals(athleteId))
                .collect(Collectors.toList());
    }

    @Override
    public List<AdjustmentProposal> findPendingByPlanId(String planId) {
        return proposals.values().stream()
                .filter(p -> p.planId().equals(planId) && p.isPending())
                .collect(Collectors.toList());
    }

    /**
     * Clear all proposals for testing purposes.
     */
    public void clearAll() {
        proposals.clear();
    }
}