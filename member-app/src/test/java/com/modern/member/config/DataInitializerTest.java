package com.modern.member.config;

import com.modern.member.model.Member;
import com.modern.member.model.Role;
import com.modern.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    void run_RepositoryEmpty_CreatesSuperAdmin() {
        when(memberRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("admin123")).thenReturn("encodedAdminPass");

        dataInitializer.run();

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());

        Member savedMember = memberCaptor.getValue();
        assertEquals("superadmin", savedMember.getUserName());
        assertEquals("encodedAdminPass", savedMember.getPassword());
        assertEquals("System Administrator", savedMember.getName());
        assertEquals("admin@member.com", savedMember.getEmail());
        assertEquals("9999999999", savedMember.getPhoneNumber());
        assertEquals(Role.ROLE_ADMIN, savedMember.getRole());
        assertEquals(1, savedMember.getTokenVersion());
        assertFalse(savedMember.isPasswordTemporary());
    }

    @Test
    void run_RepositoryNotEmpty_DoesNothing() {
        when(memberRepository.count()).thenReturn(1L);

        dataInitializer.run();

        verify(memberRepository, never()).save(any(Member.class));
    }
}