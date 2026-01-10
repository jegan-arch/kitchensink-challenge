package com.modern.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank
        String oldPassword,

        @NotBlank
        @Size(min = 6, message = "Password must be at least 6 characters")
        String newPassword
) {}