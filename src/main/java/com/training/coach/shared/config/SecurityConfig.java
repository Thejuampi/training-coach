package com.training.coach.shared.config;

import com.training.coach.security.JwtKeyProvider;
import com.training.coach.security.JwtProperties;
import com.training.coach.user.infrastructure.persistence.SystemUserJpaRepository;
import com.training.coach.user.infrastructure.persistence.UserCredentialsJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@org.springframework.boot.context.properties.EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth.pathMatchers(
                                "/",
                                "/hello",
                                "/swagger-ui/index.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/.well-known/jwks.json",
                                "/api/auth/**")
                        .permitAll()
                        .pathMatchers("/api/users/**")
                        .hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/integrations/ai/suggestions")
                        .hasAnyRole("COACH", "ADMIN")
                        .pathMatchers("/api/integrations/**")
                        .hasRole("ADMIN")
                        .pathMatchers("/api/sync/**")
                        .hasAnyRole("COACH", "ADMIN")
                        .pathMatchers("/api/training-plans/**")
                        .hasAnyRole("COACH", "ADMIN")
                        .pathMatchers("/api/analysis/**")
                        .hasAnyRole("COACH", "ADMIN")
                        .pathMatchers("/api/intensity/**")
                        .hasAnyRole("COACH", "ADMIN")
                        .pathMatchers("/api/athletes/**")
                        .hasAnyRole("COACH", "ADMIN")
                        .pathMatchers("/api/activities/**")
                        .hasAnyRole("COACH", "ATHLETE", "ADMIN")
                        .pathMatchers("/api/wellness/**")
                        .hasAnyRole("COACH", "ATHLETE", "ADMIN")
                        .pathMatchers("/api/notes/**")
                        .hasAnyRole("COACH", "ATHLETE", "ADMIN")
                        .pathMatchers("/athletes/**")
                        .hasAnyRole("COACH", "ADMIN")
                        .anyExchange()
                        .authenticated())
                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService(
            UserCredentialsJpaRepository credentialsRepository, SystemUserJpaRepository userRepository) {
        return username -> Mono.fromCallable(() -> {
                    var credentials = credentialsRepository
                            .findByUsername(username)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
                    var user = userRepository
                            .findById(credentials.getUserId())
                            .orElseThrow(() -> new UsernameNotFoundException("User profile missing for: " + username));
                    return org.springframework.security.core.userdetails.User.withUsername(credentials.getUsername())
                            .password(credentials.getPasswordHash())
                            .roles(user.getRole().name())
                            .disabled(!credentials.isEnabled())
                            .build();
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Bean
    public ReactiveUserDetailsPasswordService userDetailsPasswordService(
            UserCredentialsJpaRepository credentialsRepository) {
        return (user, newPasswordHash) -> Mono.fromCallable(() -> {
                    credentialsRepository.findByUsername(user.getUsername()).ifPresent(entity -> {
                        entity.setPasswordHash(newPasswordHash);
                        credentialsRepository.save(entity);
                    });
                    return user;
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Bean
    public JwtKeyProvider jwtKeyProvider() {
        return JwtKeyProvider.generate();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(JwtKeyProvider provider) {
        return provider.jwkSource();
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(JwtKeyProvider provider) {
        return NimbusReactiveJwtDecoder.withPublicKey(provider.publicKey()).build();
    }

    private ReactiveJwtDecoder providerReactiveJwtDecoder() {
        return NimbusReactiveJwtDecoder.withPublicKey(jwtKeyProvider().publicKey()).build();
    }

    private ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("roles");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
