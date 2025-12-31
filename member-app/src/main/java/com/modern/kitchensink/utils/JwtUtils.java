package com.modern.kitchensink.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class JwtUtils {

    private final Key key;

    public JwtUtils(@Value("${app.jwt.secret}") String jwtSecret) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public UserDetails getUserDetailsFromJwt(String token) {
        Claims claims = parseClaims(token);
        String username = claims.getSubject();

        List<SimpleGrantedAuthority> authorities = extractRoles(claims).stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        return new User(username, "", authorities);
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Claims claims) {
        return Optional.ofNullable(claims.get("roles", List.class))
                .map(list -> (List<String>) list)
                .orElse(Collections.emptyList());
    }
}