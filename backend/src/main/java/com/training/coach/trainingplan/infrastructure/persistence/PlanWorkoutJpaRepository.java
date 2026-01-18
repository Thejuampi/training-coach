package com.training.coach.trainingplan.infrastructure.persistence;

import com.training.coach.trainingplan.infrastructure.persistence.entity.PlanWorkoutEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanWorkoutJpaRepository extends JpaRepository<PlanWorkoutEntity, String> {

    List<PlanWorkoutEntity> findByPlanVersionId(String planVersionId);

    List<PlanWorkoutEntity> findByPlanVersionIdOrderByDate(String planVersionId);
}
