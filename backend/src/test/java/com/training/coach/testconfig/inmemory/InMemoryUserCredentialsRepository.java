package com.training.coach.testconfig.inmemory;

import com.training.coach.user.application.port.out.UserCredentialsRepository;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory UserCredentialsRepository for fast tests.
 */
public class InMemoryUserCredentialsRepository implements UserCredentialsRepository {
    private final ConcurrentHashMap<String, CredentialsRecord> recordsByUserId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> userIdByUsername = new ConcurrentHashMap<>();

    @Override
    public CredentialsRecord save(String userId, String username, String passwordHash, boolean enabled) {
        String id = recordsByUserId.containsKey(userId) ? recordsByUserId.get(userId).id() : java.util.UUID.randomUUID().toString();
        CredentialsRecord record = new CredentialsRecord(id, userId, username, passwordHash, enabled);
        recordsByUserId.put(userId, record);
        if (username != null) {
            userIdByUsername.put(username, userId);
        }
        return record;
    }

    @Override
    public Optional<CredentialsRecord> findByUsername(String username) {
        String userId = userIdByUsername.get(username);
        return userId == null ? Optional.empty() : Optional.ofNullable(recordsByUserId.get(userId));
    }

    @Override
    public Optional<CredentialsRecord> findByUserId(String userId) {
        return Optional.ofNullable(recordsByUserId.get(userId));
    }

    @Override
    public void updatePasswordHash(String userId, String passwordHash) {
        CredentialsRecord existing = recordsByUserId.get(userId);
        if (existing == null) {
            return;
        }
        recordsByUserId.put(userId, new CredentialsRecord(existing.id(), existing.userId(), existing.username(), passwordHash, existing.enabled()));
    }

    @Override
    public void setEnabled(String userId, boolean enabled) {
        CredentialsRecord existing = recordsByUserId.get(userId);
        if (existing == null) {
            return;
        }
        recordsByUserId.put(userId, new CredentialsRecord(existing.id(), existing.userId(), existing.username(), existing.passwordHash(), enabled));
    }
}
