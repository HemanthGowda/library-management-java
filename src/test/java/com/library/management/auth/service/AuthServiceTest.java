package com.library.management.auth.service;

import com.library.management.auth.dto.LoginRequest;
import com.library.management.auth.dto.LoginResponse;
import com.library.management.config.JwtProperties;
import com.library.management.user.model.Role;
import com.library.management.user.model.UserDocument;
import com.library.management.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        JwtService jwtService = new JwtService(new JwtProperties(
                "test-secret-key-that-is-long-enough-for-hs256-signing",
                3_600_000L,
                "library-management-test"
        ));
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void loginReturnsBearerTokenForValidCredentials() {
        UserDocument user = enabledUser();
        when(userRepository.findByEmailIgnoreCase("admin@library.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("ChangeMe123!", "hashed")).thenReturn(true);

        LoginResponse response = authService.login(new LoginRequest("admin@library.local", "ChangeMe123!"));

        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.email()).isEqualTo("admin@library.local");
        assertThat(response.roles()).containsExactly(Role.ADMIN);
        assertThat(response.expiresInMs()).isEqualTo(3_600_000L);
    }

    @Test
    void loginRejectsUnknownUser() {
        when(userRepository.findByEmailIgnoreCase("missing@library.local")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("missing@library.local", "Password1")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void loginRejectsDisabledAccount() {
        UserDocument user = enabledUser();
        user.setEnabled(false);
        when(userRepository.findByEmailIgnoreCase("admin@library.local")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin@library.local", "ChangeMe123!")))
                .isInstanceOf(DisabledException.class);
    }

    @Test
    void loginRejectsInvalidPassword() {
        UserDocument user = enabledUser();
        when(userRepository.findByEmailIgnoreCase("admin@library.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin@library.local", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    private UserDocument enabledUser() {
        UserDocument user = new UserDocument();
        user.setId("admin-1");
        user.setEmail("admin@library.local");
        user.setPasswordHash("hashed");
        user.setRoles(Set.of(Role.ADMIN));
        user.setEnabled(true);
        return user;
    }
}
