package com.training.coach.security;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

class InMemoryRefreshTokenStore implements RefreshTokenStore {

    private final Map<String, RefreshTokenRecord> byId = new ConcurrentHashMap<>();
    private final Map<String, String> idByHash = new ConcurrentHashMap<>();

    @Override
    public RefreshTokenRecord save(RefreshTokenRecord record) {
        byId.put(record.id(), record);
        idByHash.put(record.tokenHash(), record.id());
        return record;
    }

    @Override
    public Optional<RefreshTokenRecord> findByTokenHash(String tokenHash) {
        String id = idByHash.get(tokenHash);
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<RefreshTokenRecord> findByFamilyId(String familyId) {
        return byId.values().stream()
                .filter(record -> record.familyId().equals(familyId))
                .collect(Collectors.toList());
    }
}
