package com.modern.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank
        @Size(min = 3, max = 20)
        String userName,

        String role,

        @NotBlank(message = "Name is required")
        @Size(min = 1, max = 25)
        @Pattern(regexp = "[^0-9]*", message = "Name must not contain numbers")
        String name,

        @NotBlank
        @Email
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian Mobile Number")
        String phoneNumber
) {}