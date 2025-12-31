package com.modern.Auth.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        String code,
        String message,
        LocalDateTime timestamp
) {
    public ErrorResponse(String code, String message, int status) {
        this(status, code, message, LocalDateTime.now());
    }
}