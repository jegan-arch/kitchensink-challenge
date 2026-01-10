package com.modern.member.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modern.member.dto.ErrorResponse;
import com.modern.member.exception.ErrorCode;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(@Nonnull HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {

        log.error("Unauthorized error: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.SESSION_EXPIRED.getCode(),
                ErrorCode.SESSION_EXPIRED.getMessage(),
                HttpServletResponse.SC_UNAUTHORIZED
        );

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}