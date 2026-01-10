package com.modern.member.exception;

import com.modern.member.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleBusinessException_ReturnsCorrectErrorCode() {
        BusinessException ex = new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(ErrorCode.USERNAME_ALREADY_EXISTS.getCode(), response.getBody().code());
        assertEquals(ErrorCode.USERNAME_ALREADY_EXISTS.getMessage(), response.getBody().message());
    }

    @Test
    void handleAccessDenied_ReturnsForbidden() {
        AccessDeniedException ex = new AccessDeniedException("Not allowed");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDenied(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(ErrorCode.ACCESS_DENIED.getCode(), response.getBody().code());
    }

    @Test
    void handleBadCredentials_ReturnsUnauthorized() {
        BadCredentialsException ex = new BadCredentialsException("Bad creds");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadCredentials(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(ErrorCode.AUTHENTICATION_FAILED.getCode(), response.getBody().code());
    }

    @Test
    void handleValidationExceptions_ReturnsBadRequestWithCombinedMessages() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("object", "field1", "Error 1");
        FieldError fieldError2 = new FieldError("object", "field2", "Error 2");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ErrorCode.VALIDATION_FAILED.getCode(), response.getBody().code());
        assertEquals("Error 1, Error 2", response.getBody().message());
    }

    @Test
    void handleGeneral_ReturnsInternalServerError() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGeneral(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), response.getBody().code());
    }
}