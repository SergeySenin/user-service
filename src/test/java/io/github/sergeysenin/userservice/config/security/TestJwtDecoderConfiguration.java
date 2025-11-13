package io.github.sergeysenin.userservice.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.List;

@Configuration
@Profile("test")
public class TestJwtDecoderConfiguration {

    private static final Instant ISSUED_AT = Instant.parse("2025-01-01T00:00:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2025-01-01T01:00:00Z");

    @Bean
    @Primary
    public JwtDecoder testJwtDecoder() {
        return token -> Jwt.withTokenValue(token)
                .header("alg", "RS256")
                .header("typ", "JWT")
                .header("kid", "test-key-id")
                .issuer("http://localhost:9999/realms/test")
                .audience(List.of("user-service"))
                .subject("test-user-id")
                .claim("user_id", 1L)
                .claim("preferred_username", "test_user")
                .claim("given_name", "Test")
                .claim("family_name", "User")
                .claim("email", "test.user@example.com")
                .claim("roles", List.of("USER"))
                .issuedAt(ISSUED_AT)
                .expiresAt(EXPIRES_AT)
                .build();
    }
}
