package com.example.authapp.service;

import com.example.authapp.dto.request.LoginRequest;
import com.example.authapp.dto.request.LogoutRequest;
import com.example.authapp.dto.request.RefreshRequest;
import com.example.authapp.dto.request.RegisterRequest;
import com.example.authapp.dto.response.AuthResponse;
import com.example.authapp.dto.response.RegisterResponse;
import com.example.authapp.entity.RefreshToken;
import com.example.authapp.entity.Role;
import com.example.authapp.entity.User;
import com.example.authapp.exception.AppException;
import com.example.authapp.repository.RefreshTokenRepository;
import com.example.authapp.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenHashService tokenHashService;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            TokenHashService tokenHashService
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenHashService = tokenHashService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new AppException(HttpStatus.CONFLICT, "USERNAME_EXISTS", "username already exists");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new AppException(HttpStatus.CONFLICT, "EMAIL_EXISTS", "email already exists");
        }

        User user = new User();
        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        userRepository.save(user);

        return new RegisterResponse("register success");
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(request.identifier(), request.identifier())
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "invalid credentials");
        }

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        Claims claims;
        try {
            claims = jwtService.parseAndValidate(request.refreshToken(), JwtService.TOKEN_TYPE_REFRESH);
        } catch (JwtException ex) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "refresh token is invalid");
        }

        String tokenHash = tokenHashService.hash(request.refreshToken());
        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "refresh token is invalid"));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED", "refresh token expired or revoked");
        }

        Long userIdFromToken = Long.parseLong(claims.getSubject());
        if (!stored.getUser().getId().equals(userIdFromToken)) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "refresh token is invalid");
        }

        stored.setRevoked(true);
        stored.setLastUsedAt(Instant.now());
        refreshTokenRepository.save(stored);

        return issueTokens(stored.getUser());
    }

    @Transactional
    public void logout(LogoutRequest request) {
        String tokenHash = tokenHashService.hash(request.refreshToken());
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshToken.setLastUsedAt(Instant.now());
            refreshTokenRepository.save(refreshToken);
        });
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getRole());

        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setTokenHash(tokenHashService.hash(refreshToken));
        refreshTokenEntity.setExpiresAt(jwtService.refreshTokenExpiryFromNow());
        refreshTokenEntity.setRevoked(false);
        refreshTokenEntity.setLastUsedAt(Instant.now());
        refreshTokenRepository.save(refreshTokenEntity);

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.accessTokenTtlSeconds(),
                user.getRole().name()
        );
    }
}
