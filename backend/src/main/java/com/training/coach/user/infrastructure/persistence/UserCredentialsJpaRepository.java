package com.training.coach.user.infrastructure.persistence;

import com.training.coach.user.infrastructure.persistence.entity.UserCredentialsEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCredentialsJpaRepository extends JpaRepository<UserCredentialsEntity, String> {
    Optional<UserCredentialsEntity> findByUsername(String username);

    Optional<UserCredentialsEntity> findByUserId(String userId);
}
