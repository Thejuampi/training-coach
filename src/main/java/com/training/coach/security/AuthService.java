package com.training.coach.security;

import com.training.coach.user.application.port.out.SystemUserRepository;
import com.training.coach.user.domain.model.SystemUser;
import com.training.coach.user.infrastructure.persistence.UserCredentialsJpaRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtProperties properties;
    private final JwtEncoder encoder;
    private final RefreshTokenStore refreshTokenStore;
    private final UserCredentialsJpaRepository credentialsRepository;
    private final SystemUserRepository userRepository;

    public AuthService(
            PasswordEncoder passwordEncoder,
            JwtProperties properties,
            JwtEncoder encoder,
            RefreshTokenStore refreshTokenStore,
            UserCredentialsJpaRepository credentialsRepository,
            SystemUserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
        this.encoder = encoder;
        this.refreshTokenStore = refreshTokenStore;
        this.credentialsRepository = credentialsRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AuthTokens authenticate(String username, String password) {
        var credentials = credentialsRepository
                .findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!credentials.isEnabled()) {
            throw new IllegalArgumentException("User is disabled");
        }
        if (!passwordEncoder.matches(password, credentials.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        var user = userRepository
                .findById(credentials.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String accessToken = issueAccessToken(user);
        IssuedRefreshToken refreshToken =
                issueRefreshToken(user.id(), UUID.randomUUID().toString());
        return new AuthTokens(accessToken, refreshToken.raw(), properties.accessTokenTtlSeconds());
    }

    @Transactional
    public AuthTokens refresh(String refreshToken) {
        String hash = hash(refreshToken);
        RefreshTokenStore.RefreshTokenRecord token = refreshTokenStore
                .findByTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        if (token.revokedAt() != null) {
            revokeFamily(token.familyId());
            throw new IllegalArgumentException("Refresh token revoked");
        }
        if (token.expiresAt().isBefore(Instant.now())) {
            token = token.withRevokedAt(Instant.now());
            refreshTokenStore.save(token);
            throw new IllegalArgumentException("Refresh token expired");
        }

        SystemUser user = loadUser(token.userId());
        IssuedRefreshToken newRefreshToken = issueRefreshToken(user.id(), token.familyId());
        token = token.withRevokedAt(Instant.now()).withReplacedBy(newRefreshToken.recordId());
        refreshTokenStore.save(token);

        return new AuthTokens(issueAccessToken(user), newRefreshToken.raw(), properties.accessTokenTtlSeconds());
    }

    @Transactional
    public void logout(String refreshToken, boolean allSessions) {
        String hash = hash(refreshToken);
        RefreshTokenStore.RefreshTokenRecord token = refreshTokenStore
                .findByTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        if (allSessions) {
            revokeFamily(token.familyId());
            return;
        }
        token = token.withRevokedAt(Instant.now());
        refreshTokenStore.save(token);
    }

    @Transactional(readOnly = true)
    public SystemUser loadUser(String userId) {
        return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private void revokeFamily(String familyId) {
        List<RefreshTokenStore.RefreshTokenRecord> tokens = refreshTokenStore.findByFamilyId(familyId);
        for (RefreshTokenStore.RefreshTokenRecord token : tokens) {
            if (token.revokedAt() == null) {
                refreshTokenStore.save(token.withRevokedAt(Instant.now()));
            }
        }
    }

    private String issueAccessToken(SystemUser user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .audience(List.of(properties.audience()))
                .subject(user.id())
                .claim("roles", List.of(user.role().name()))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(properties.accessTokenTtlSeconds()))
                .id(UUID.randomUUID().toString())
                .build();
        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    private IssuedRefreshToken issueRefreshToken(String userId, String familyId) {
        String raw = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        String recordId = UUID.randomUUID().toString();
        RefreshTokenStore.RefreshTokenRecord record = new RefreshTokenStore.RefreshTokenRecord(
                recordId,
                userId,
                hash(raw),
                familyId,
                Instant.now().plus(properties.refreshTokenTtlDays(), ChronoUnit.DAYS),
                null,
                null);
        refreshTokenStore.save(record);
        return new IssuedRefreshToken(raw, recordId);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] result = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(result);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to hash token", ex);
        }
    }

    public record AuthTokens(String accessToken, String refreshToken, long expiresInSeconds) {}

    private record IssuedRefreshToken(String raw, String recordId) {}
}
