package com.training.coach.user.infrastructure.persistence;

import com.training.coach.user.infrastructure.persistence.entity.SystemUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemUserJpaRepository extends JpaRepository<SystemUserEntity, String> {}
