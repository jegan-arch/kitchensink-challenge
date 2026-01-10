package com.modern.member.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.modern.member.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AuthEntryPointJwtTest {

    private AuthEntryPointJwt authEntryPoint;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        authEntryPoint = new AuthEntryPointJwt(objectMapper);
    }

    @Test
    void commence_ShouldSetUnauthorizedResponseWithJsonBody() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException authException = new AuthenticationException("Test Auth Exception") {};

        authEntryPoint.commence(request, response, authException);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        String responseBody = response.getContentAsString();

        assertTrue(responseBody.contains(ErrorCode.SESSION_EXPIRED.getCode()));
        assertTrue(responseBody.contains(String.valueOf(HttpServletResponse.SC_UNAUTHORIZED)));
    }
}