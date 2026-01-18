package com.training.coach.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.training.coach.user.application.port.out.SystemUserRepository;
import com.training.coach.user.domain.model.SystemUser;
import com.training.coach.user.domain.model.UserPreferences;
import com.training.coach.user.domain.model.UserRole;
import com.training.coach.user.infrastructure.persistence.UserCredentialsJpaRepository;
import com.training.coach.user.infrastructure.persistence.entity.UserCredentialsEntity;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

class AuthServiceTest {

    @Test
    void authenticateIssuesTokens() {
        var passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        var jwtProps = JwtProperties.defaults();
        var keyProvider = JwtKeyProvider.forTests();

        var credentialsRepo = org.mockito.Mockito.mock(UserCredentialsJpaRepository.class);
        var userRepo = org.mockito.Mockito.mock(SystemUserRepository.class);
        var refreshRepo = new InMemoryRefreshTokenStore();

        UserCredentialsEntity cred = new UserCredentialsEntity();
        cred.setUserId("u-1");
        cred.setUsername("coach_a");
        cred.setPasswordHash(passwordEncoder.encode("secret"));
        cred.setEnabled(true);
        when(credentialsRepo.findByUsername("coach_a")).thenReturn(Optional.of(cred));

        SystemUser user = new SystemUser("u-1", "Coach A", UserRole.COACH, UserPreferences.metricDefaults());
        when(userRepo.findById("u-1")).thenReturn(Optional.of(user));

        AuthService service = new AuthService(
                passwordEncoder, jwtProps, keyProvider.encoder(), refreshRepo, credentialsRepo, userRepo);

        AuthService.AuthTokens tokens = service.authenticate("coach_a", "secret");
        assertThat(tokens.accessToken()).isNotBlank();
        assertThat(tokens.refreshToken()).isNotBlank();
        assertThat(tokens.expiresInSeconds()).isPositive();
    }

    @Test
    void authenticateRejectsInvalidPassword() {
        var passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        var jwtProps = JwtProperties.defaults();
        var keyProvider = JwtKeyProvider.forTests();

        var credentialsRepo = org.mockito.Mockito.mock(UserCredentialsJpaRepository.class);
        var userRepo = org.mockito.Mockito.mock(SystemUserRepository.class);
        var refreshRepo = new InMemoryRefreshTokenStore();

        UserCredentialsEntity cred = new UserCredentialsEntity();
        cred.setUserId("u-1");
        cred.setUsername("coach_a");
        cred.setPasswordHash(passwordEncoder.encode("secret"));
        cred.setEnabled(true);
        when(credentialsRepo.findByUsername("coach_a")).thenReturn(Optional.of(cred));

        AuthService service = new AuthService(
                passwordEncoder, jwtProps, keyProvider.encoder(), refreshRepo, credentialsRepo, userRepo);

        assertThatThrownBy(() -> service.authenticate("coach_a", "wrong")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refreshRotatesToken() {
        var passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        var jwtProps = JwtProperties.defaults();
        var keyProvider = JwtKeyProvider.forTests();
        var credentialsRepo = org.mockito.Mockito.mock(UserCredentialsJpaRepository.class);
        var userRepo = org.mockito.Mockito.mock(SystemUserRepository.class);
        var refreshRepo = new InMemoryRefreshTokenStore();

        UserCredentialsEntity cred = new UserCredentialsEntity();
        cred.setUserId("u-1");
        cred.setUsername("coach_a");
        cred.setPasswordHash(passwordEncoder.encode("secret"));
        cred.setEnabled(true);
        when(credentialsRepo.findByUsername("coach_a")).thenReturn(Optional.of(cred));

        SystemUser user = new SystemUser("u-1", "Coach A", UserRole.COACH, UserPreferences.metricDefaults());
        when(userRepo.findById("u-1")).thenReturn(Optional.of(user));

        AuthService service = new AuthService(
                passwordEncoder, jwtProps, keyProvider.encoder(), refreshRepo, credentialsRepo, userRepo);

        AuthService.AuthTokens issued = service.authenticate("coach_a", "secret");
        AuthService.AuthTokens tokens = service.refresh(issued.refreshToken());

        assertThat(tokens.refreshToken()).isNotEqualTo(issued.refreshToken());
        RefreshTokenStore.RefreshTokenRecord revoked =
                refreshRepo.findByTokenHash(hash(issued.refreshToken())).orElseThrow();
        assertThat(revoked.revokedAt()).isNotNull();
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
}
