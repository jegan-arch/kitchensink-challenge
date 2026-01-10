package com.modern.member.dto;

public record JwtResponse(
        String token,
        String type,
        String id,
        String userName,
        String email,
        String role,
        boolean isPasswordTemporary
) {}