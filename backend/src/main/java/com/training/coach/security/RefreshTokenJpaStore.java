package com.training.coach.security;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!test")
public class RefreshTokenJpaStore implements RefreshTokenStore {

    private final RefreshTokenRepository repository;

    public RefreshTokenJpaStore(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public RefreshTokenRecord save(RefreshTokenRecord record) {
        RefreshTokenEntity entity = repository.findById(record.id()).orElseGet(RefreshTokenEntity::new);
        entity.setId(record.id());
        entity.setUserId(record.userId());
        entity.setTokenHash(record.tokenHash());
        entity.setFamilyId(record.familyId());
        entity.setExpiresAt(record.expiresAt());
        entity.setRevokedAt(record.revokedAt());
        entity.setReplacedBy(record.replacedBy());
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
