package com.training.coach.security;

import com.training.coach.common.AuthTokens;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokens> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request.username(), request.password()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokens> refresh(@RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken(), request.allSessions());
        return ResponseEntity.noContent().build();
    }

    public record LoginRequest(String username, String password) {}

    public record RefreshRequest(String refreshToken) {}

    public record LogoutRequest(String refreshToken, boolean allSessions) {}
}
