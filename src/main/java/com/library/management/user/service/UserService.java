package com.library.management.user.service;

import com.library.management.exception.ConflictException;
import com.library.management.user.dto.RegisterUserRequest;
import com.library.management.user.dto.UserResponse;
import com.library.management.user.model.Role;
import com.library.management.user.model.UserDocument;
import com.library.management.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    public UserResponse register(RegisterUserRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("Email is already registered");
        }

        UserDocument user = new UserDocument();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setRoles(Set.of(Role.MEMBER));
        user.setEnabled(true);

        UserDocument saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
