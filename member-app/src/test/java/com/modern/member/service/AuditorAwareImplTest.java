package com.modern.member.service;

import com.modern.member.service.impl.AuditorAwareImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditorAwareImplTest {

    private AuditorAwareImpl auditorAware;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        auditorAware = new AuditorAwareImpl();
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentAuditor_AuthenticatedUser_ReturnsUsername() {
        String username = "testuser";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);

        Optional<String> result = auditorAware.getCurrentAuditor();

        assertTrue(result.isPresent());
        assertEquals(username, result.get());
    }

    @Test
    void getCurrentAuditor_NoAuthentication_ReturnsEmpty() {
        when(securityContext.getAuthentication()).thenReturn(null);

        Optional<String> result = auditorAware.getCurrentAuditor();

        assertFalse(result.isPresent());
    }

    @Test
    void getCurrentAuditor_AnonymousUser_ReturnsEmptyOrNullName() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(null);

        Optional<String> result = auditorAware.getCurrentAuditor();

        assertFalse(result.isPresent());
    }
}