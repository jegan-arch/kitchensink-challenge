package com.modern.Auth.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USERNAME_ALREADY_TAKEN("AUTH-001", "Username is already taken", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_IN_USE("AUTH-002", "Email is already in use", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS("AUTH-003", "Invalid username or password", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED("AUTH-004", "Account is locked due to too many failed attempts", HttpStatus.FORBIDDEN),

    INTERNAL_SERVER_ERROR("SYS-001", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_FAILED("SYS-002", "Input validation failed", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}