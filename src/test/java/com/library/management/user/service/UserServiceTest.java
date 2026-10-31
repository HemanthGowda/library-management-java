package com.library.management.user.service;

import com.library.management.exception.ConflictException;
import com.library.management.user.dto.RegisterUserRequest;
import com.library.management.user.dto.UserResponse;
import com.library.management.user.model.Role;
import com.library.management.user.model.UserDocument;
import com.library.management.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, new UserMapper());
    }

    @Test
    void registerCreatesMemberWithNormalizedEmail() {
        RegisterUserRequest request = new RegisterUserRequest(
                "  Alex@Example.com ",
                "Password1",
                " Alex Reader "
        );

        when(userRepository.existsByEmailIgnoreCase("alex@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("hashed");
        when(userRepository.save(any(UserDocument.class))).thenAnswer(invocation -> {
            UserDocument document = invocation.getArgument(0);
            document.setId("user-1");
            document.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
            return document;
        });

        UserResponse response = userService.register(request);

        ArgumentCaptor<UserDocument> captor = ArgumentCaptor.forClass(UserDocument.class);
        verify(userRepository).save(captor.capture());

        UserDocument saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("alex@example.com");
        assertThat(saved.getFullName()).isEqualTo("Alex Reader");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed");
        assertThat(saved.getRoles()).isEqualTo(Set.of(Role.MEMBER));
        assertThat(saved.isEnabled()).isTrue();

        assertThat(response.id()).isEqualTo("user-1");
        assertThat(response.email()).isEqualTo("alex@example.com");
        assertThat(response.roles()).containsExactly(Role.MEMBER);
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("alex@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(
                new RegisterUserRequest("alex@example.com", "Password1", "Alex Reader")
        ))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already registered");
    }
}
