package com.modern.Auth.utils;

import com.modern.Auth.security.utils.JwtUtils;
import com.modern.Auth.service.impl.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.Key;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    private static final String TEST_SECRET = "TmV3U2VjcmV0S2V5Rm9ySldUVGVzdGluZ1B1cnBvc2VzMTIzNDU2Nzg5";
    private static final int TEST_EXPIRATION_MS = 60000;

    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(TEST_SECRET, TEST_EXPIRATION_MS);
    }

    @Test
    void generateJwtToken_ShouldReturnValidToken_WithCorrectClaims() {
        String expectedUsername = "testuser";
        String expectedRole = "ROLE_USER";

        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getUsername()).thenReturn(expectedUsername);
        doReturn(List.of(new SimpleGrantedAuthority(expectedRole)))
                .when(userDetails).getAuthorities();

        when(authentication.getPrincipal()).thenReturn(userDetails);

        String token = jwtUtils.generateJwtToken(authentication);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals(expectedUsername, claims.getSubject());

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles");
        assertNotNull(roles);
        assertTrue(roles.contains(expectedRole));
    }
}