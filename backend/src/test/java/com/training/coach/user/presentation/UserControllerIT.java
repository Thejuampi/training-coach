package com.training.coach.user.presentation;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

import com.training.coach.testconfig.AuthTestConfig;
import com.training.coach.testconfig.WebTestConfig;
import com.training.coach.user.application.port.out.SystemUserRepository;
import com.training.coach.user.application.port.out.UserCredentialsRepository;
import com.training.coach.user.domain.model.SystemUser;
import com.training.coach.user.domain.model.UserPreferences;
import com.training.coach.user.domain.model.UserRole;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import com.training.coach.AbstractWebFluxControllerTest;

@SpringBootTest(properties = "intervals.icu.api-key=test", webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import({WebTestConfig.class, AuthTestConfig.class})
class UserControllerIT extends AbstractWebFluxControllerTest {

    @Autowired
    private SystemUserRepository systemUserRepository;

    @Autowired
    private UserCredentialsRepository userCredentialsRepository;

    @Test
    void getCredentialsSummaryDoesNotExposePassword() {
        String userId = "user-1";
        String username = "coach_a";
        String passwordHash = "hashed";
        SystemUser user = new SystemUser(userId, "Coach A", UserRole.COACH, UserPreferences.metricDefaults());
        systemUserRepository.save(user);
        userCredentialsRepository.save(userId, username, passwordHash, true);

        webTestClient
                .mutateWith(mockUser().roles("ADMIN"))
                .get()
                .uri("/api/users/{id}/credentials", userId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.userId")
                .isEqualTo(userId)
                .jsonPath("$.username")
                .isEqualTo(username)
                .jsonPath("$.enabled")
                .isEqualTo(true)
                .jsonPath("$.passwordHash")
                .doesNotExist();
    }
}
