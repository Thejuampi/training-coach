package com.training.coach.user.application.service;

import com.training.coach.shared.functional.Result;
import com.training.coach.user.application.port.out.SystemUserRepository;
import com.training.coach.user.application.port.out.UserCredentialsRepository;
import com.training.coach.user.domain.model.SystemUser;
import com.training.coach.user.domain.model.UserPreferences;
import com.training.coach.user.domain.model.UserRole;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.security.SecureRandom;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SystemUserService {

    private static final Logger logger = LoggerFactory.getLogger(SystemUserService.class);

    private final SystemUserRepository repository;
    private final UserCredentialsRepository credentialsRepository;
    private final PasswordEncoder passwordEncoder;

    public SystemUserService(
            SystemUserRepository repository,
            UserCredentialsRepository credentialsRepository,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.credentialsRepository = credentialsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Result<SystemUser> createUser(
            String name, UserRole role, UserPreferences preferences, String username, String password) {
        if (username != null && !username.isBlank()) {
            if (credentialsRepository.findByUsername(username).isPresent()) {
                return Result.failure(new IllegalArgumentException("Username already exists: " + username));
            }
        }
        String id = UUID.randomUUID().toString();
        SystemUser user = new SystemUser(id, name, role, preferences);
        SystemUser saved = repository.save(user);
        if (username != null && !username.isBlank() && password != null && !password.isBlank()) {
            String hash = passwordEncoder.encode(password);
            credentialsRepository.save(saved.id(), username, hash, true);
        }
        logger.info("audit.user.create userId={} role={} usernameSet={}", saved.id(), role, username != null);
        return Result.success(saved);
    }

    public Result<Void> setPassword(String userId, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            return Result.failure(new IllegalArgumentException("Password cannot be blank"));
        }
        if (credentialsRepository.findByUserId(userId).isEmpty()) {
            return Result.failure(new IllegalArgumentException("Credentials not found for: " + userId));
        }
        credentialsRepository.updatePasswordHash(userId, passwordEncoder.encode(newPassword));
        logger.info("audit.user.password.set userId={}", userId);
        return Result.success(null);
    }

    public Result<String> rotatePassword(String userId) {
        if (credentialsRepository.findByUserId(userId).isEmpty()) {
            return Result.failure(new IllegalArgumentException("Credentials not found for: " + userId));
        }
        String generated = generatePassword();
        credentialsRepository.updatePasswordHash(userId, passwordEncoder.encode(generated));
        logger.info("audit.user.password.rotate userId={}", userId);
        return Result.success(generated);
    }

    public Result<Void> disableUser(String userId) {
        if (credentialsRepository.findByUserId(userId).isEmpty()) {
            return Result.failure(new IllegalArgumentException("Credentials not found for: " + userId));
        }
        credentialsRepository.setEnabled(userId, false);
        logger.info("audit.user.disable userId={}", userId);
        return Result.success(null);
    }

    public Result<Void> enableUser(String userId) {
        if (credentialsRepository.findByUserId(userId).isEmpty()) {
            return Result.failure(new IllegalArgumentException("Credentials not found for: " + userId));
        }
        credentialsRepository.setEnabled(userId, true);
        logger.info("audit.user.enable userId={}", userId);
        return Result.success(null);
    }

    public Result<UserCredentialsSummary> getCredentialsSummary(String userId) {
        return credentialsRepository
                .findByUserId(userId)
                .map(record -> Result.success(new UserCredentialsSummary(
                        record.userId(), record.username(), record.enabled())))
                .orElse(Result.failure(new IllegalArgumentException("Credentials not found for: " + userId)));
    }

    public record UserCredentialsSummary(String userId, String username, boolean enabled) {}

    private String generatePassword() {
        byte[] bytes = new byte[18];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
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
