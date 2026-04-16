package com.example.authapp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "username is required")
        @Size(min = 3, max = 40, message = "username length must be 3-40")
        String username,

        @NotBlank(message = "email is required")
        @Email(message = "email format is invalid")
        @Size(max = 120, message = "email is too long")
        String email,

        @NotBlank(message = "password is required")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,64}$",
                message = "password must be 8-64 chars with upper/lower letters and digits"
        )
        String password
) {}
