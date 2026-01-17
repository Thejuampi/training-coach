package com.training.coach.security;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenJpaStore implements RefreshTokenStore {

    private final RefreshTokenRepository repository;

    public RefreshTokenJpaStore(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public RefreshTokenRecord save(RefreshTokenRecord record) {
        RefreshTokenEntity entity = repository.findById(record.getId()).orElseGet(RefreshTokenEntity::new);
        entity.setId(record.getId());
        entity.setUserId(record.getUserId());
        entity.setTokenHash(record.getTokenHash());
        entity.setFamilyId(record.getFamilyId());
        entity.setExpiresAt(record.getExpiresAt());
        entity.setRevokedAt(record.getRevokedAt());
        entity.setReplacedBy(record.getReplacedBy());
        return toRecord(repository.save(entity));
    }

    @Override
    public Optional<RefreshTokenRecord> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(this::toRecord);
    }

    @Override
    public List<RefreshTokenRecord> findByFamilyId(String familyId) {
        return repository.findByFamilyId(familyId).stream().map(this::toRecord).collect(Collectors.toList());
    }

    private RefreshTokenRecord toRecord(RefreshTokenEntity entity) {
        return new RefreshTokenRecord(
                entity.getId(),
                entity.getUserId(),
                entity.getTokenHash(),
                entity.getFamilyId(),
                entity.getExpiresAt(),
                entity.getRevokedAt(),
                entity.getReplacedBy());
    }
}
