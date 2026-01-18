package com.training.coach.security;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenStore {
    RefreshTokenRecord save(RefreshTokenRecord record);

    Optional<RefreshTokenRecord> findByTokenHash(String tokenHash);

    List<RefreshTokenRecord> findByFamilyId(String familyId);

    record RefreshTokenRecord(
            String id,
            String userId,
            String tokenHash,
            String familyId,
            Instant expiresAt,
            Instant revokedAt,
            String replacedBy) {
        public RefreshTokenRecord withRevokedAt(Instant revokedAt) {
            return new RefreshTokenRecord(id, userId, tokenHash, familyId, expiresAt, revokedAt, replacedBy);
        }

        public RefreshTokenRecord withReplacedBy(String replacedBy) {
            return new RefreshTokenRecord(id, userId, tokenHash, familyId, expiresAt, revokedAt, replacedBy);
        }
    }
}
