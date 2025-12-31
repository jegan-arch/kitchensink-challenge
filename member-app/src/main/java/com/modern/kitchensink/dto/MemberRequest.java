package com.modern.kitchensink.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MemberRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
        String phoneNumber
) {}