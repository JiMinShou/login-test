package com.example.authapp.dto.response;

public record UserProfileResponse(
        Long id,
        String username,
        String email,
        String role
) {}
