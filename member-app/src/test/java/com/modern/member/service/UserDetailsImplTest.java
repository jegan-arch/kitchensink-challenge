package com.modern.member.service;

import com.modern.member.model.Member;
import com.modern.member.model.Role;
import com.modern.member.service.impl.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailsImplTest {

    @Test
    void build_ShouldMapMemberToUserDetailsCorrectly() {
        Member member = new Member();
        member.setId("123");
        member.setUserName("testUser");
        member.setEmail("test@example.com");
        member.setPassword("encodedPass");
        member.setRole(Role.ROLE_ADMIN);
        member.setTokenVersion(5);
        member.setPasswordTemporary(true);
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());
        UserDetailsImpl userDetails = UserDetailsImpl.build(member);
        assertNotNull(userDetails);
        assertEquals("123", userDetails.getId());
        assertEquals("testUser", userDetails.getUsername());
        assertEquals("test@example.com", userDetails.getEmail());
        assertEquals("encodedPass", userDetails.getPassword());
        assertEquals(5, userDetails.getTokenVersion());
        assertTrue(userDetails.isPasswordTemporary());
    }

    @Test
    void build_ShouldMapAuthoritiesCorrectly() {
        Member member = new Member();
        member.setId("1");
        member.setRole(Role.ROLE_USER);
        member.setUserName("user");
        member.setPassword("pass");

        UserDetailsImpl userDetails = UserDetailsImpl.build(member);

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertNotNull(authorities);
        assertEquals(1, authorities.size());

        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
    }
}