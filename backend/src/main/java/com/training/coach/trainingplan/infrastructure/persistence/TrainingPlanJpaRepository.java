package com.training.coach.trainingplan.infrastructure.persistence;

import com.training.coach.trainingplan.infrastructure.persistence.entity.TrainingPlanEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingPlanJpaRepository extends JpaRepository<TrainingPlanEntity, String> {

    List<TrainingPlanEntity> findByAthleteId(String athleteId);
}
