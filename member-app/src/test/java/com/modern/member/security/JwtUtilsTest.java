package com.modern.member.security;

import com.modern.member.service.impl.UserDetailsImpl;
import com.modern.member.utils.JwtUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final int TEST_EXPIRATION = 60000;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(TEST_SECRET, TEST_EXPIRATION);
    }

    @Test
    void generateJwtToken_Success() {
        UserDetailsImpl userDetails = new UserDetailsImpl(
                "1", "testuser", "test@email.com", "pass", false, 5,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        String token = jwtUtils.generateJwtToken(authentication);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals("testuser", jwtUtils.getUserNameFromJwtToken(token));
        assertEquals(5, jwtUtils.getVersionFromJwtToken(token));
    }

    @Test
    void validateJwtToken_Success() {
        String token = createTestToken("testuser", TEST_EXPIRATION);
        assertTrue(jwtUtils.validateJwtToken(token));
    }

    @Test
    void validateJwtToken_Expired_ReturnsFalse() throws InterruptedException {
        String expiredToken = createTestToken("expiredUser", 1);

        Thread.sleep(10);

        assertFalse(jwtUtils.validateJwtToken(expiredToken));
    }

    @Test
    void validateJwtToken_Malformed_ReturnsFalse() {
        assertFalse(jwtUtils.validateJwtToken("invalid.token.string"));
    }

    @Test
    void validateJwtToken_Empty_ReturnsFalse() {
        assertFalse(jwtUtils.validateJwtToken(""));
    }

    @Test
    void validateJwtToken_InvalidSignature_ReturnsFalse() {
        String differentSecret = "505E635266556A586E3272357538782F413F4428472B4B6250645367566B5971";
        Key differentKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(differentSecret));

        String forgedToken = Jwts.builder()
                .setSubject("hacker")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TEST_EXPIRATION))
                .signWith(differentKey, SignatureAlgorithm.HS256)
                .compact();

        assertFalse(jwtUtils.validateJwtToken(forgedToken));
    }

    @Test
    void validateJwtToken_Unsupported_ReturnsFalse() {
        String unsupportedToken = "eyJhbGciOiJub25lIn0.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.";
        assertFalse(jwtUtils.validateJwtToken(unsupportedToken));
    }

    private String createTestToken(String subject, int expirationMs) {
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}