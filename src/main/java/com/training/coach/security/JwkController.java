package com.training.coach.security;

import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JwkController {

    private final JwtKeyProvider keyProvider;

    public JwkController(JwtKeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    @GetMapping("/.well-known/jwks.json")
    public Object jwks() {
        return keyProvider.jwkSet().toJSONObject();
    }
}
