package com.training.coach.shared.config;

import com.training.coach.user.application.port.out.UserCredentialsRepository;
import com.training.coach.user.application.service.SystemUserService;
import com.training.coach.user.domain.model.UserPreferences;
import com.training.coach.user.domain.model.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class SecurityBootstrap {

    @Bean
    public CommandLineRunner bootstrapAdmin(
            UserCredentialsRepository credentialsRepository, SystemUserService userService) {
        return args -> {
            if (credentialsRepository.findByUsername(defaultAdminUsername()).isPresent()) {
                return;
            }
            userService.createUser(
                    "Admin",
                    UserRole.ADMIN,
                    UserPreferences.metricDefaults(),
                    defaultAdminUsername(),
                    defaultAdminPassword());
        };
    }

    private String defaultAdminUsername() {
        return System.getenv().getOrDefault("SECURITY_BOOTSTRAP_ADMIN_USERNAME", "admin");
    }

    private String defaultAdminPassword() {
        return System.getenv().getOrDefault("SECURITY_BOOTSTRAP_ADMIN_PASSWORD", "adminpass");
    }
}
