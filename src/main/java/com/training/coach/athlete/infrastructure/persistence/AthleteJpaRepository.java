package com.training.coach.athlete.infrastructure.persistence;

import com.training.coach.athlete.infrastructure.persistence.entity.AthleteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for AthleteEntity.
 */
public interface AthleteJpaRepository extends JpaRepository<AthleteEntity, String> {}
