package com.modern.kitchensink.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    MEMBER_NOT_FOUND("MEM-001", "Member not found", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_IN_USE("MEM-002", "Email is already registered", HttpStatus.BAD_REQUEST),
    INVALID_PHONE_FORMAT("MEM-003", "Phone number must be a valid Indian number (starts with 6-9)", HttpStatus.BAD_REQUEST),

    INTERNAL_SERVER_ERROR("SYS-001", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_FAILED("SYS-002", "Input validation failed", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED("SYS-003", "You do not have permission to access this resource", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}