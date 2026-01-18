package com.training.coach.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

class AuthControllerTest {

    private WebTestClient webTestClient;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);
        webTestClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    void loginReturnsUnauthorizedForInvalidCredentials() {
        AuthController.LoginRequest request = new AuthController.LoginRequest("coach_a", "wrong");
        when(authService.authenticate("coach_a", "wrong"))
                .thenThrow(new AuthUnauthorizedException("Invalid credentials"));

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void loginReturnsUnauthorizedForDisabledUser() {
        AuthController.LoginRequest request = new AuthController.LoginRequest("coach_a", "secret");
        when(authService.authenticate("coach_a", "secret"))
                .thenThrow(new AuthUnauthorizedException("User is disabled"));

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }
}
