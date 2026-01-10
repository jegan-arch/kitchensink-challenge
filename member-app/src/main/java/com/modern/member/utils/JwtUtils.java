package com.modern.member.utils;

import com.modern.member.service.impl.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtils {

    private final Key key;
    private final int jwtExpirationMs;
    private final JwtParser jwtParser;

    public JwtUtils(@Value("${app.jwt.secret}") String jwtSecret,
                    @Value("${app.jwt.expiration-ms}") int jwtExpirationMs) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        this.jwtExpirationMs = jwtExpirationMs;
        this.jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
    }

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = Objects.requireNonNull(userPrincipal).getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("roles", roles)
                .claim("version", userPrincipal.getTokenVersion())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public int getVersionFromJwtToken(String token) {
        Integer version = extractClaim(token, claims -> claims.get("version", Integer.class));
        return version != null ? version : 0;
    }

    public boolean validateJwtToken(String authToken) {
        try {
            jwtParser.parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = jwtParser.parseClaimsJws(token).getBody();
        return claimsResolver.apply(claims);
    }
}