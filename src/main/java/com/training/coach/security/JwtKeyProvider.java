package com.training.coach.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

public class JwtKeyProvider {

    private final RSAKey rsaKey;

    public JwtKeyProvider(RSAKey rsaKey) {
        this.rsaKey = rsaKey;
    }

    public static JwtKeyProvider generate() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            RSAKey key = new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(UUID.randomUUID().toString())
                    .build();
            return new JwtKeyProvider(key);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate RSA key pair", ex);
        }
    }

    public static JwtKeyProvider forTests() {
        return generate();
    }

    public JWKSet jwkSet() {
        return new JWKSet(rsaKey);
    }

    public JWKSource<SecurityContext> jwkSource() {
        return new ImmutableJWKSet<>(jwkSet());
    }

    public JwtEncoder encoder() {
        return new NimbusJwtEncoder(jwkSource());
    }
}
