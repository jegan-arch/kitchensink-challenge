package com.modern.member.service;

import com.modern.member.model.Member;
import com.modern.member.model.Role;
import com.modern.member.repository.MemberRepository;
import com.modern.member.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_Success() {
        String username = "testuser";
        Member member = new Member();
        member.setId("1");
        member.setUserName(username);
        member.setEmail("test@email.com");
        member.setPassword("encodedPass");
        member.setRole(Role.ROLE_USER);

        when(memberRepository.findByUserName(username)).thenReturn(Optional.of(member));

        UserDetails result = userDetailsService.loadUserByUsername(username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals("encodedPass", result.getPassword());
        verify(memberRepository).findByUserName(username);
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsException() {
        String username = "nonexistent";
        when(memberRepository.findByUserName(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername(username)
        );

        verify(memberRepository).findByUserName(username);
    }
}