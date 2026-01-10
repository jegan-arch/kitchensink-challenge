package com.modern.member.exception;

import com.modern.member.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        return buildResponseEntity(ex.getErrorCode());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return buildResponseEntity(ErrorCode.ACCESS_DENIED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return buildResponseEntity(ErrorCode.AUTHENTICATION_FAILED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String combinedErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return buildResponseEntity(ErrorCode.VALIDATION_FAILED, combinedErrors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unexpected Error: ", ex);
        return buildResponseEntity(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildResponseEntity(ErrorCode ec) {
        return buildResponseEntity(ec, ec.getMessage());
    }

    private ResponseEntity<ErrorResponse> buildResponseEntity(ErrorCode ec, String messageOverride) {
        ErrorResponse error = new ErrorResponse(
                ec.getCode(),
                messageOverride,
                ec.getHttpStatus().value()
        );
        return new ResponseEntity<>(error, ec.getHttpStatus());
    }
}