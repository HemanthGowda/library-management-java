package com.library.management.auth.dto;

import com.library.management.user.model.Role;

import java.util.Set;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInMs,
        String userId,
        String email,
        Set<Role> roles
) {
}
