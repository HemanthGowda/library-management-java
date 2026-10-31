package com.library.management.user.service;

import com.library.management.user.dto.UserResponse;
import com.library.management.user.model.UserDocument;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(UserDocument document) {
        return new UserResponse(
                document.getId(),
                document.getEmail(),
                document.getFullName(),
                document.getRoles(),
                document.isEnabled(),
                document.getCreatedAt()
        );
    }
}
