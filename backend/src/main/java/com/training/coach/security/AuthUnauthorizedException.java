package com.training.coach.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AuthUnauthorizedException extends ResponseStatusException {

    public AuthUnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
