package com.library.management.auth.service;

import com.library.management.config.JwtProperties;
import com.library.management.user.model.Role;
import com.library.management.user.model.UserDocument;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = buildKey(jwtProperties.secret());
    }

    public String generateToken(UserDocument user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(jwtProperties.expirationMs());

        return Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(user.getId())
                .claim("email", user.getEmail())
                .claim("roles", user.getRoles().stream().map(Enum::name).toList())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(jwtProperties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Set<Role> extractRoles(Claims claims) {
        Object rolesClaim = claims.get("roles");
        if (!(rolesClaim instanceof List<?> roles)) {
            return Set.of();
        }

        return roles.stream()
                .map(Object::toString)
                .map(Role::valueOf)
                .collect(Collectors.toSet());
    }

    public long getExpirationMs() {
        return jwtProperties.expirationMs();
    }

    private SecretKey buildKey(String secret) {
        byte[] keyBytes = decodeSecret(secret);

        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 256 bits (32 bytes)");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    private byte[] decodeSecret(String secret) {
        try {
            return Decoders.BASE64.decode(secret);
        } catch (RuntimeException ex) {
            return secret.getBytes(StandardCharsets.UTF_8);
        }
    }
}
