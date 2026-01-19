package com.training.coach.testconfig;

import com.training.coach.security.RefreshTokenStore;
import com.training.coach.testconfig.inmemory.InMemoryRefreshTokenStore;
import com.training.coach.testconfig.inmemory.InMemorySystemUserRepository;
import com.training.coach.testconfig.inmemory.InMemoryUserCredentialsRepository;
import com.training.coach.user.application.port.out.SystemUserRepository;
import com.training.coach.user.application.port.out.UserCredentialsRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/**
 * Test configuration for auth/user security beans.
 *
 * <p>Use this config when tests exercise auth flows with in-memory stores.</p>
 */
@TestConfiguration
@Profile("test")
public class AuthTestConfig {

    @Bean
    public SystemUserRepository systemUserRepository() {
        return new InMemorySystemUserRepository();
    }

    @Bean
    public UserCredentialsRepository userCredentialsRepository() {
        return new InMemoryUserCredentialsRepository();
    }

    @Bean
    public RefreshTokenStore refreshTokenStore() {
        return new InMemoryRefreshTokenStore();
    }
}
