package com.training.coach.user.presentation;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

import com.training.coach.shared.functional.Result;
import com.training.coach.user.application.service.SystemUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(properties = "intervals.icu.api-key=test")
@AutoConfigureWebTestClient
@Import(UserControllerTest.TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private SystemUserService userService;

    @Test
    void getCredentialsSummaryDoesNotExposePassword() {
        String userId = "user-1";
        SystemUserService.UserCredentialsSummary summary =
                new SystemUserService.UserCredentialsSummary(userId, "coach_a", true);

        when(userService.getCredentialsSummary(userId)).thenReturn(Result.success(summary));

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
                .isEqualTo("coach_a")
                .jsonPath("$.enabled")
                .isEqualTo(true)
                .jsonPath("$.passwordHash")
                .doesNotExist();
    }

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        @Order(0)
        SecurityWebFilterChain testSecurityWebFilterChain(ServerHttpSecurity http) {
            return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                    .authorizeExchange(exchange -> exchange.anyExchange().permitAll())
                    .build();
        }
    }
}
