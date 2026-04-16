package com.example.authapp.service;

import com.example.authapp.dto.request.LoginRequest;
import com.example.authapp.dto.request.RegisterRequest;
import com.example.authapp.dto.response.AuthResponse;
import com.example.authapp.entity.Role;
import com.example.authapp.entity.User;
import com.example.authapp.exception.AppException;
import com.example.authapp.repository.RefreshTokenRepository;
import com.example.authapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenHashService tokenHashService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerSuccess() {
        when(userRepository.existsByUsernameIgnoreCase("alice")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Passw0rdA")).thenReturn("encoded");

        authService.register(new RegisterRequest("alice", "alice@example.com", "Passw0rdA"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getUsername()).isEqualTo("alice");
        assertThat(saved.getEmail()).isEqualTo("alice@example.com");
        assertThat(saved.getPasswordHash()).isEqualTo("encoded");
        assertThat(saved.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void registerDuplicateEmailShouldFail() {
        when(userRepository.existsByUsernameIgnoreCase("alice")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("alice", "alice@example.com", "Passw0rdA")))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("email already exists");
    }

    @Test
    void loginSuccess() {
        User user = new User();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setPasswordHash("encoded");
        user.setRole(Role.USER);

        setEntityId(user, 11L);

        when(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("alice", "alice"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Passw0rdA", "encoded")).thenReturn(true);
        when(jwtService.generateAccessToken(11L, Role.USER)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(11L, Role.USER)).thenReturn("refresh-token");
        when(jwtService.refreshTokenExpiryFromNow()).thenReturn(java.time.Instant.now().plusSeconds(3600));
        when(jwtService.accessTokenTtlSeconds()).thenReturn(900L);
        when(tokenHashService.hash(any())).thenReturn("hashed-token");

        AuthResponse response = authService.login(new LoginRequest("alice", "Passw0rdA"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(refreshTokenRepository, atLeastOnce()).save(any());
    }

    @Test
    void loginWrongPassword() {
        User user = new User();
        user.setPasswordHash("encoded");

        when(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("alice", "alice"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("alice", "wrong")))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("invalid credentials");
    }

    private static void setEntityId(User user, Long id) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
