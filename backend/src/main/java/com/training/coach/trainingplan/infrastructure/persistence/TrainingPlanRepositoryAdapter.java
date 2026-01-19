package com.training.coach.trainingplan.infrastructure.persistence;

import com.training.coach.athlete.domain.model.TrainingPlan;
import com.training.coach.trainingplan.application.port.out.TrainingPlanRepository;
import com.training.coach.trainingplan.domain.model.TrainingPlanSummary;
import com.training.coach.trainingplan.infrastructure.persistence.entity.PlanVersionEntity;
import com.training.coach.trainingplan.infrastructure.persistence.entity.PlanVersionStatus;
import com.training.coach.trainingplan.infrastructure.persistence.entity.TrainingPlanEntity;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * JPA adapter for TrainingPlanRepository.
 */
@Component
@Profile("!test")
public class TrainingPlanRepositoryAdapter implements TrainingPlanRepository {

    private final TrainingPlanJpaRepository trainingPlanJpaRepository;
    private final PlanVersionJpaRepository planVersionJpaRepository;
    private final PlanWorkoutJpaRepository planWorkoutJpaRepository;

    public TrainingPlanRepositoryAdapter(
            TrainingPlanJpaRepository trainingPlanJpaRepository,
            PlanVersionJpaRepository planVersionJpaRepository,
            PlanWorkoutJpaRepository planWorkoutJpaRepository) {
        this.trainingPlanJpaRepository = trainingPlanJpaRepository;
        this.planVersionJpaRepository = planVersionJpaRepository;
        this.planWorkoutJpaRepository = planWorkoutJpaRepository;
    }

    @Override
    public TrainingPlan save(TrainingPlan plan) {
        // For now, assume plan is saved via version and workouts
        // This is a simplified implementation
        return plan;
    }

    @Override
    public Optional<TrainingPlan> findById(String id) {
        // Simplified - would need to reconstruct from entities
        return Optional.empty();
    }

    @Override
    public void deleteById(String id) {
        trainingPlanJpaRepository.deleteById(id);
    }

    @Override
    public List<TrainingPlanSummary> findByAthleteId(String athleteId) {
        return trainingPlanJpaRepository.findByAthleteId(athleteId).stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    private TrainingPlanSummary toSummary(TrainingPlanEntity entity) {
        List<PlanVersionEntity> versions = planVersionJpaRepository.findByPlanIdOrderByVersionDesc(entity.getId());
        if (versions.isEmpty()) {
            return new TrainingPlanSummary(
                    entity.getId(), entity.getAthleteId(), entity.getTitle(), 0, PlanVersionStatus.DRAFT);
        }
        PlanVersionEntity latest = versions.get(0);
        return new TrainingPlanSummary(
                entity.getId(), entity.getAthleteId(), entity.getTitle(), latest.getVersion(), latest.getStatus());
    }
}
