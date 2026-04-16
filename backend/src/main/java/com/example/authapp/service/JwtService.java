package com.example.authapp.service;

import com.example.authapp.config.JwtProperties;
import com.example.authapp.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = buildSecretKey(properties.getSecret());
    }

    public String generateAccessToken(Long userId, Role role) {
        Instant now = Instant.now();
        Instant expiry = now.plus(properties.getAccessTokenMinutes(), ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuer(properties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim("type", TOKEN_TYPE_ACCESS)
                .claim("role", role.name())
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(Long userId, Role role) {
        Instant now = Instant.now();
        Instant expiry = now.plus(properties.getRefreshTokenDays(), ChronoUnit.DAYS);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .issuer(properties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim("type", TOKEN_TYPE_REFRESH)
                .claim("role", role.name())
                .signWith(secretKey)
                .compact();
    }

    public Claims parseAndValidate(String token, String expectedType) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String type = claims.get("type", String.class);
        if (!expectedType.equals(type)) {
            throw new JwtException("invalid token type");
        }
        return claims;
    }

    public Instant refreshTokenExpiryFromNow() {
        return Instant.now().plus(properties.getRefreshTokenDays(), ChronoUnit.DAYS);
    }

    public long accessTokenTtlSeconds() {
        return properties.getAccessTokenMinutes() * 60;
    }

    private SecretKey buildSecretKey(String configuredSecret) {
        if (configuredSecret == null || configuredSecret.isBlank()) {
            throw new IllegalStateException("app.jwt.secret must be configured");
        }

        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(configuredSecret);
        } catch (IllegalArgumentException ex) {
            keyBytes = configuredSecret.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret is too short. Use at least 32 bytes");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
