package com.training.coach.trainingplan.infrastructure.persistence;

import com.training.coach.trainingplan.infrastructure.persistence.entity.PlanVersionEntity;
import com.training.coach.trainingplan.infrastructure.persistence.entity.PlanVersionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanVersionJpaRepository extends JpaRepository<PlanVersionEntity, String> {

    Optional<PlanVersionEntity> findFirstByPlanIdOrderByVersionDesc(String planId);

    List<PlanVersionEntity> findByPlanIdOrderByVersionDesc(String planId);

    Optional<PlanVersionEntity> findByPlanIdAndVersion(String planId, Integer version);

    List<PlanVersionEntity> findByPlanIdAndStatus(String planId, PlanVersionStatus status);
}
