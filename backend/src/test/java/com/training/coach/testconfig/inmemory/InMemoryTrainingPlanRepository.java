package com.training.coach.testconfig.inmemory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.training.coach.athlete.domain.model.TrainingPlan;
import com.training.coach.trainingplan.application.port.out.TrainingPlanRepository;
import com.training.coach.trainingplan.domain.model.TrainingPlanSummary;
import com.training.coach.trainingplan.infrastructure.persistence.entity.PlanVersionStatus;

/**
 * In-memory TrainingPlanRepository for fast tests.
 */
public class InMemoryTrainingPlanRepository implements TrainingPlanRepository {
    private final ConcurrentHashMap<String, TrainingPlan> plans = new ConcurrentHashMap<>();

    @Override
    public TrainingPlan save(TrainingPlan plan) {
        plans.put(plan.id(), plan);
        return plan;
    }

    @Override
    public Optional<TrainingPlan> findById(String id) {
        return Optional.ofNullable(plans.get(id));
    }

    @Override
    public void deleteById(String id) {
        plans.remove(id);
    }

    @Override
    public List<TrainingPlanSummary> findByAthleteId(String athleteId) {
        return plans.values().stream()
                .filter(plan -> plan.athleteId().equals(athleteId))
                .map(plan -> new TrainingPlanSummary(plan.id(), plan.athleteId(), "Plan " + plan.id().substring(0, 8), 1, PlanVersionStatus.PUBLISHED))
                .toList();
    }
}
