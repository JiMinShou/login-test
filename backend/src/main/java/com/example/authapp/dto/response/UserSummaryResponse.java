package com.example.authapp.dto.response;

import java.time.Instant;

public record UserSummaryResponse(
        Long id,
        String username,
        String email,
        String role,
        Instant createdAt
) {}
