package com.modern.Auth.dto;

import java.util.List;

public record JwtResponse(
        String token,
        String id,
        String username,
        String email,
        List<String> roles,
        String type
) {
    public JwtResponse(String token, String id, String username, String email, List<String> roles) {
        this(token, id, username, email, roles, "Bearer");
    }
}