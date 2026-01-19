package com.training.coach.user.infrastructure.persistence;

import com.training.coach.user.application.port.out.SystemUserRepository;
import com.training.coach.user.domain.model.SystemUser;
import com.training.coach.user.domain.model.UserPreferences;
import com.training.coach.user.infrastructure.persistence.entity.SystemUserEntity;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class SystemUserRepositoryAdapter implements SystemUserRepository {

    private final SystemUserJpaRepository repository;

    public SystemUserRepositoryAdapter(SystemUserJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public SystemUser save(SystemUser user) {
        SystemUserEntity entity = toEntity(user);
        SystemUserEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<SystemUser> findById(String id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public java.util.List<SystemUser> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    private SystemUserEntity toEntity(SystemUser user) {
        SystemUserEntity entity = new SystemUserEntity();
        entity.setId(user.id());
        entity.setName(user.name());
        entity.setRole(user.role());
        entity.setMeasurementSystem(user.preferences().measurementSystem());
        entity.setWeightUnit(user.preferences().weightUnit());
        entity.setDistanceUnit(user.preferences().distanceUnit());
        entity.setHeightUnit(user.preferences().heightUnit());
        entity.setActivityVisibility(user.preferences().activityVisibility());
        entity.setWellnessDataSharing(user.preferences().wellnessDataSharing());
        return entity;
    }

    private SystemUser toDomain(SystemUserEntity entity) {
        UserPreferences preferences = new UserPreferences(
                entity.getMeasurementSystem(),
                entity.getWeightUnit(),
                entity.getDistanceUnit(),
                entity.getHeightUnit(),
                entity.getActivityVisibility(),
                entity.getWellnessDataSharing());
        return new SystemUser(entity.getId(), entity.getName(), entity.getRole(), preferences);
    }
}
