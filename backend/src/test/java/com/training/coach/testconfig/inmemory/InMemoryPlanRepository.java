package com.training.coach.testconfig.inmemory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.training.coach.trainingplan.application.port.out.PlanRepository;
import com.training.coach.trainingplan.domain.model.PlanSummary;
import com.training.coach.trainingplan.domain.model.PlanVersion;
import com.training.coach.trainingplan.infrastructure.persistence.entity.PlanVersionStatus;

/**
 * In-memory PlanRepository for fast tests.
 */
public class InMemoryPlanRepository implements PlanRepository {
    private final ConcurrentHashMap<String, PlanSummary> plans = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<PlanVersion>> versionsByPlan = new ConcurrentHashMap<>();

    @Override
    public PlanSummary save(PlanSummary plan) {
        plans.put(plan.id(), plan);
        return plan;
    }

    @Override
    public Optional<PlanSummary> findById(String id) {
        return Optional.ofNullable(plans.get(id));
    }

    @Override
    public List<PlanSummary> findAll() {
        return List.copyOf(plans.values());
    }

    @Override
    public PlanVersion saveVersion(PlanVersion version) {
        versionsByPlan.computeIfAbsent(version.planId(), key -> new java.util.ArrayList<>()).add(version);
        return version;
    }

    @Override
    public Optional<PlanVersion> findVersion(String planId, int version) {
        return Optional.ofNullable(versionsByPlan.get(planId))
                .flatMap(list -> list.stream().filter(v -> v.versionNumber() == version).findFirst());
    }

    @Override
    public List<PlanVersion> findVersions(String planId) {
        return versionsByPlan.getOrDefault(planId, List.of());
    }

    @Override
    public void updateVersionStatus(String planId, int version, PlanVersionStatus status) {
        List<PlanVersion> versions = versionsByPlan.get(planId);
        if (versions == null) {
            return;
        }
        for (int index = 0; index < versions.size(); index++) {
            PlanVersion existing = versions.get(index);
            if (existing.versionNumber() == version) {
                versions.set(index, new PlanVersion(existing.planId(), existing.versionNumber(), status, existing.workouts(), existing.createdAt()));
                return;
            }
        }
    }
}
