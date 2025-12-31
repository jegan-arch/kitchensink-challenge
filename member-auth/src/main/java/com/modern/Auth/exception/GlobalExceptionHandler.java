package com.modern.Auth.exception;

import com.modern.Auth.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(KitchensinkException.class)
    public ResponseEntity<ErrorResponse> handleKitchensinkException(KitchensinkException ex) {
        ErrorCode ec = ex.getErrorCode();

        ErrorResponse error = new ErrorResponse(
                ec.getCode(),
                ec.getMessage(),
                ec.getHttpStatus().value()
        );
        return new ResponseEntity<>(error, ec.getHttpStatus());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthError(AuthenticationException ex) {
        ErrorCode ec = ErrorCode.INVALID_CREDENTIALS;
        return new ResponseEntity<>(
                new ErrorResponse(ec.getCode(), ec.getMessage(), ec.getHttpStatus().value()),
                ec.getHttpStatus()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                details.put(error.getField(), error.getDefaultMessage()));

        ErrorCode ec = ErrorCode.VALIDATION_FAILED;

        return new ResponseEntity<>(details, ec.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        ErrorCode ec = ErrorCode.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(
                new ErrorResponse(ec.getCode(), ec.getMessage(), ec.getHttpStatus().value()),
                ec.getHttpStatus()
        );
    }
}