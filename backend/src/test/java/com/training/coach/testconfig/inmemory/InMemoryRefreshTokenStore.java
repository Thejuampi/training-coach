package com.training.coach.testconfig.inmemory;

import com.training.coach.security.RefreshTokenStore;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory RefreshTokenStore for fast tests.
 */
public class InMemoryRefreshTokenStore implements RefreshTokenStore {
    private final ConcurrentHashMap<String, RefreshTokenRecord> tokens = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, java.util.List<RefreshTokenRecord>> familyIndex = new ConcurrentHashMap<>();

    @Override
    public RefreshTokenRecord save(RefreshTokenRecord record) {
        tokens.put(record.tokenHash(), record);
        familyIndex.computeIfAbsent(record.familyId(), key -> new java.util.ArrayList<>())
                .removeIf(existing -> existing.tokenHash().equals(record.tokenHash()));
        familyIndex.get(record.familyId()).add(record);
        return record;
    }

    @Override
    public Optional<RefreshTokenRecord> findByTokenHash(String tokenHash) {
        return Optional.ofNullable(tokens.get(tokenHash));
    }

    @Override
    public List<RefreshTokenRecord> findByFamilyId(String familyId) {
        return familyIndex.getOrDefault(familyId, List.of());
    }
}
