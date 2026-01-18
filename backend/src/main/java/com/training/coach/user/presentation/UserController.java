package com.training.coach.user.presentation;

import com.training.coach.user.application.service.SystemUserService;
import com.training.coach.user.domain.model.SystemUser;
import com.training.coach.user.domain.model.UserPreferences;
import com.training.coach.user.domain.model.UserRole;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final SystemUserService userService;

    public UserController(SystemUserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<SystemUser> createUser(@RequestBody CreateUserRequest request) {
        var result = userService.createUser(
                request.name(), request.role(), request.preferences(), request.username(), request.password());
        if (result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result.value().get());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SystemUser> getUser(@PathVariable String id) {
        var result = userService.getUser(id);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result.value().get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<SystemUser>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}/preferences")
    public ResponseEntity<UserPreferences> getUserPreferences(@PathVariable String id) {
        var result = userService.getUser(id);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result.value().get().preferences());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{id}/credentials")
    public ResponseEntity<SystemUserService.UserCredentialsSummary> getCredentials(@PathVariable String id) {
        var result = userService.getCredentialsSummary(id);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result.value().get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}/preferences")
    public ResponseEntity<SystemUser> updateUserPreferences(
            @PathVariable String id, @RequestBody UserPreferences preferences) {
        var result = userService.updatePreferences(id, preferences);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result.value().get());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/{id}/password")
    public ResponseEntity<Void> setPassword(@PathVariable String id, @RequestBody PasswordRequest request) {
        var result = userService.setPassword(id, request.newPassword());
        if (result.isSuccess()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/{id}/password/rotate")
    public ResponseEntity<RotatePasswordResponse> rotatePassword(@PathVariable String id) {
        var result = userService.rotatePassword(id);
        if (result.isSuccess()) {
            return ResponseEntity.ok(new RotatePasswordResponse(result.value().get()));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/{id}/disable")
    public ResponseEntity<Void> disableUser(@PathVariable String id) {
        var result = userService.disableUser(id);
        if (result.isSuccess()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/{id}/enable")
    public ResponseEntity<Void> enableUser(@PathVariable String id) {
        var result = userService.enableUser(id);
        if (result.isSuccess()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    public record CreateUserRequest(
            String name, UserRole role, UserPreferences preferences, String username, String password) {}

    public record PasswordRequest(String newPassword) {}

    public record RotatePasswordResponse(String password) {}
}
