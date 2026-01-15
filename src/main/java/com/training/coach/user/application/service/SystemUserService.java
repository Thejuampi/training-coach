package com.training.coach.user.application.service;

import com.training.coach.shared.functional.Result;
import com.training.coach.user.application.port.out.SystemUserRepository;
import com.training.coach.user.domain.model.SystemUser;
import com.training.coach.user.domain.model.UserPreferences;
import com.training.coach.user.domain.model.UserRole;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SystemUserService {

    private final SystemUserRepository repository;

    public SystemUserService(SystemUserRepository repository) {
        this.repository = repository;
    }

    public Result<SystemUser> createUser(String name, UserRole role, UserPreferences preferences) {
        String id = UUID.randomUUID().toString();
        SystemUser user = new SystemUser(id, name, role, preferences);
        return Result.success(repository.save(user));
    }

    public Result<SystemUser> updatePreferences(String id, UserPreferences preferences) {
        return repository
                .findById(id)
                .map(existing -> Result.success(
                        repository.save(new SystemUser(id, existing.name(), existing.role(), preferences))))
                .orElse(Result.failure(new IllegalArgumentException("User not found: " + id)));
    }

    public Result<SystemUser> getUser(String id) {
        return repository
                .findById(id)
                .map(Result::success)
                .orElse(Result.failure(new IllegalArgumentException("User not found: " + id)));
    }

    public java.util.List<SystemUser> getAllUsers() {
        return repository.findAll();
    }
}
