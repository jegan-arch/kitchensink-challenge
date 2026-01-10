package com.modern.member.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    MEMBER_NOT_FOUND("MEM-001", "Member not found", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_IN_USE("MEM-002", "Email is already registered", HttpStatus.CONFLICT),
    USERNAME_ALREADY_EXISTS("MEM-003", "Username is already taken", HttpStatus.CONFLICT),
    INVALID_PHONE_FORMAT("MEM-004", "Phone number must be a valid Indian number (starts with 6-9)", HttpStatus.BAD_REQUEST),
    PHONE_NUMBER_IN_USE("MEM-005", "Phone number is already registered", HttpStatus.CONFLICT),
    CANNOT_DELETE_LAST_ADMIN("MEM-006", "Cannot delete the last administrator. Promote another user first.", HttpStatus.FORBIDDEN),
    CANNOT_DOWNGRADE_LAST_ADMIN( "MEM-007","Cannot downgrade the last administrator. Promote another user first.", HttpStatus.FORBIDDEN),
    ROLE_NOT_FOUND("MEM-08", "Role not found, Available roles are ROLE_USER, ROLE_ADMIN", HttpStatus.BAD_REQUEST),

    AUTHENTICATION_FAILED("SEC-001", "Invalid username or password", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("SEC-002", "You do not have permission to access this resource", HttpStatus.FORBIDDEN),
    SESSION_EXPIRED("SEC-003", "Session has expired or been invalidated, Please Login again!", HttpStatus.UNAUTHORIZED),

    VALIDATION_FAILED("SYS-001", "Input validation failed", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("SYS-002", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}