package com.training.coach.user.infrastructure.persistence;

import com.training.coach.user.application.port.out.UserCredentialsRepository;
import com.training.coach.user.infrastructure.persistence.entity.UserCredentialsEntity;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class UserCredentialsRepositoryAdapter implements UserCredentialsRepository {

    private final UserCredentialsJpaRepository repository;

    public UserCredentialsRepositoryAdapter(UserCredentialsJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public CredentialsRecord save(String userId, String username, String passwordHash, boolean enabled) {
        UserCredentialsEntity entity = new UserCredentialsEntity();
        entity.setUserId(userId);
        entity.setUsername(username);
        entity.setPasswordHash(passwordHash);
        entity.setEnabled(enabled);
        return toRecord(repository.save(entity));
    }

    @Override
    public Optional<CredentialsRecord> findByUsername(String username) {
        return repository.findByUsername(username).map(this::toRecord);
    }

    @Override
    public Optional<CredentialsRecord> findByUserId(String userId) {
        return repository.findByUserId(userId).map(this::toRecord);
    }

    @Override
    public void updatePasswordHash(String userId, String passwordHash) {
        UserCredentialsEntity entity = repository.findByUserId(userId).orElseThrow();
        entity.setPasswordHash(passwordHash);
        repository.save(entity);
    }

    @Override
    public void setEnabled(String userId, boolean enabled) {
        UserCredentialsEntity entity = repository.findByUserId(userId).orElseThrow();
        entity.setEnabled(enabled);
        repository.save(entity);
    }

    private CredentialsRecord toRecord(UserCredentialsEntity entity) {
        return new CredentialsRecord(
                entity.getId(), entity.getUserId(), entity.getUsername(), entity.getPasswordHash(), entity.isEnabled());
    }
}
