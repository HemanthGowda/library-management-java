package com.library.management.user.dto;

import com.library.management.user.model.Role;

import java.time.Instant;
import java.util.Set;

public record UserResponse(
        String id,
        String email,
        String fullName,
        Set<Role> roles,
        boolean enabled,
        Instant createdAt
) {
}
