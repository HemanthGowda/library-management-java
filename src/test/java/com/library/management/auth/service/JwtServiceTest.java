package com.library.management.auth.service;

import com.library.management.config.JwtProperties;
import com.library.management.user.model.Role;
import com.library.management.user.model.UserDocument;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(new JwtProperties(
                "test-secret-key-that-is-long-enough-for-hs256-signing",
                3_600_000L,
                "library-management-test"
        ));
    }

    @Test
    void generateAndParseTokenRoundTrip() {
        UserDocument user = new UserDocument();
        user.setId("user-42");
        user.setEmail("admin@library.local");
        user.setRoles(Set.of(Role.ADMIN, Role.LIBRARIAN));

        String token = jwtService.generateToken(user);
        Claims claims = jwtService.parseClaims(token);

        assertThat(claims.getSubject()).isEqualTo("user-42");
        assertThat(claims.get("email", String.class)).isEqualTo("admin@library.local");
        assertThat(jwtService.extractRoles(claims)).containsExactlyInAnyOrder(Role.ADMIN, Role.LIBRARIAN);
        assertThat(jwtService.getExpirationMs()).isEqualTo(3_600_000L);
    }

    @Test
    void rejectsShortSecret() {
        assertThatThrownBy(() -> new JwtService(new JwtProperties("too-short", 1000L, "issuer")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("256 bits");
    }
}
