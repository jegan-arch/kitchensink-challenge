package com.modern.kitchensink.exception;

import com.modern.kitchensink.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(KitchensinkException.class)
    public ResponseEntity<ErrorResponse> handleKitchensinkException(KitchensinkException ex) {
        return buildResponseEntity(ex.getErrorCode());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return buildResponseEntity(ErrorCode.ACCESS_DENIED);
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
        return buildResponseEntity(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildResponseEntity(ErrorCode ec) {
        ErrorResponse error = new ErrorResponse(
                ec.getCode(),
                ec.getMessage(),
                ec.getHttpStatus().value()
        );
        return new ResponseEntity<>(error, ec.getHttpStatus());
    }
}