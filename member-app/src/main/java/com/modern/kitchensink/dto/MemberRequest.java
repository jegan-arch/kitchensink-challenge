package com.modern.kitchensink.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 1, max = 25, message = "Name must be between 1 and 25 characters")
        @Pattern(regexp = "[^0-9]*", message = "Name must not contain numbers")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone number must be valid 10-digit Indian number")
        String phoneNumber
) {}