package com.modern.kitchensink.security;

import com.modern.kitchensink.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private AuthTokenFilter authTokenFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WhenValidToken_ShouldSetAuthentication() throws ServletException, IOException {
        String token = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());

        when(jwtUtils.getUserDetailsFromJwt(token)).thenReturn(userDetails);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("testuser", SecurityContextHolder.getContext().getAuthentication().getName());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WhenNoToken_ShouldNotSetAuthentication() throws ServletException, IOException {

        lenient().when(jwtUtils.getUserDetailsFromJwt(null)).thenThrow(new IllegalArgumentException("Token is null"));

        authTokenFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WhenInvalidToken_ShouldCatchExceptionAndContinue() throws ServletException, IOException {
        String token = "invalid.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtils.getUserDetailsFromJwt(token)).thenThrow(new RuntimeException("Invalid Token"));

        authTokenFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
    }

    private void assertNotNull(Object object) {
        if (object == null) {
            throw new AssertionError("Expected object to not be null");
        }
    }
}