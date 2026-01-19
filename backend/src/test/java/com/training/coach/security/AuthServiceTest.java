package com.training.coach.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.training.coach.common.AuthTokens;
import com.training.coach.security.AuthUnauthorizedException;
import com.training.coach.user.application.port.out.SystemUserRepository;
import com.training.coach.user.domain.model.SystemUser;
import com.training.coach.user.domain.model.UserPreferences;
import com.training.coach.user.domain.model.UserRole;
import com.training.coach.user.application.port.out.UserCredentialsRepository;
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

        var credentialsRepo = org.mockito.Mockito.mock(UserCredentialsRepository.class);
        var userRepo = org.mockito.Mockito.mock(SystemUserRepository.class);
        var refreshRepo = new InMemoryRefreshTokenStore();

        UserCredentialsRepository.CredentialsRecord cred =
                new UserCredentialsRepository.CredentialsRecord(
                        "cred-1", "u-1", "coach_a", passwordEncoder.encode("secret"), true);
        when(credentialsRepo.findByUsername("coach_a")).thenReturn(Optional.of(cred));

        SystemUser user = new SystemUser("u-1", "Coach A", UserRole.COACH, UserPreferences.metricDefaults());
        when(userRepo.findById("u-1")).thenReturn(Optional.of(user));

        AuthService service = new AuthService(
                passwordEncoder, jwtProps, keyProvider.encoder(), refreshRepo, credentialsRepo, userRepo);

        AuthTokens tokens = service.authenticate("coach_a", "secret");
        assertThat(tokens.accessToken()).isNotBlank();
        assertThat(tokens.refreshToken()).isNotBlank();
        assertThat(tokens.expiresInSeconds()).isPositive();
    }

    @Test
    void authenticateRejectsInvalidPassword() {
        var passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        var jwtProps = JwtProperties.defaults();
        var keyProvider = JwtKeyProvider.forTests();

        var credentialsRepo = org.mockito.Mockito.mock(UserCredentialsRepository.class);
        var userRepo = org.mockito.Mockito.mock(SystemUserRepository.class);
        var refreshRepo = new InMemoryRefreshTokenStore();

        UserCredentialsRepository.CredentialsRecord cred =
                new UserCredentialsRepository.CredentialsRecord(
                        "cred-1", "u-1", "coach_a", passwordEncoder.encode("secret"), true);
        when(credentialsRepo.findByUsername("coach_a")).thenReturn(Optional.of(cred));

        AuthService service = new AuthService(
                passwordEncoder, jwtProps, keyProvider.encoder(), refreshRepo, credentialsRepo, userRepo);

        assertThatThrownBy(() -> service.authenticate("coach_a", "wrong"))
                .isInstanceOf(AuthUnauthorizedException.class);
    }

    @Test
    void refreshRotatesToken() {
        var passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        var jwtProps = JwtProperties.defaults();
        var keyProvider = JwtKeyProvider.forTests();
        var credentialsRepo = org.mockito.Mockito.mock(UserCredentialsRepository.class);
        var userRepo = org.mockito.Mockito.mock(SystemUserRepository.class);
        var refreshRepo = new InMemoryRefreshTokenStore();

        UserCredentialsRepository.CredentialsRecord cred =
                new UserCredentialsRepository.CredentialsRecord(
                        "cred-1", "u-1", "coach_a", passwordEncoder.encode("secret"), true);
        when(credentialsRepo.findByUsername("coach_a")).thenReturn(Optional.of(cred));

        SystemUser user = new SystemUser("u-1", "Coach A", UserRole.COACH, UserPreferences.metricDefaults());
        when(userRepo.findById("u-1")).thenReturn(Optional.of(user));

        AuthService service = new AuthService(
                passwordEncoder, jwtProps, keyProvider.encoder(), refreshRepo, credentialsRepo, userRepo);

        AuthTokens issued = service.authenticate("coach_a", "secret");
        AuthTokens tokens = service.refresh(issued.refreshToken());

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
