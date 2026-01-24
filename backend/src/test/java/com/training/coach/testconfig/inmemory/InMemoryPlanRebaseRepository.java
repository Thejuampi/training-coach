package com.training.coach.testconfig.inmemory;

import com.training.coach.trainingplan.application.port.out.PlanRebaseRepository;
import com.training.coach.trainingplan.domain.model.PlanRebase;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory PlanRebaseRepository for fast tests.
 */
public class InMemoryPlanRebaseRepository implements PlanRebaseRepository {
    private final ConcurrentHashMap<String, PlanRebase> rebases = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<PlanRebase>> rebasesByPlanId = new ConcurrentHashMap<>();

    @Override
    public PlanRebase save(PlanRebase rebase) {
        rebases.put(rebase.id(), rebase);
        rebasesByPlanId.computeIfAbsent(rebase.planId(), k -> new java.util.ArrayList<>()).add(rebase);
        return rebase;
    }

    @Override
    public List<PlanRebase> findByPlanId(String planId) {
        return rebasesByPlanId.getOrDefault(planId, List.of());
    }

    @Override
    public PlanRebase findById(String rebaseId) {
        return rebases.get(rebaseId);
    }

    /**
     * Clear all rebase records for testing purposes.
     */
    public void clearAll() {
        rebases.clear();
        rebasesByPlanId.clear();
    }
}