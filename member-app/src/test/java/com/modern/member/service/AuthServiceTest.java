package com.modern.member.service;

import com.modern.member.dto.JwtResponse;
import com.modern.member.dto.LoginRequest;
import com.modern.member.dto.MemberResponse;
import com.modern.member.dto.SignupRequest;
import com.modern.member.exception.BusinessException;
import com.modern.member.exception.ErrorCode;
import com.modern.member.model.Member;
import com.modern.member.model.Role;
import com.modern.member.repository.MemberRepository;
import com.modern.member.service.impl.AuthServiceImpl;
import com.modern.member.service.impl.UserDetailsImpl;
import com.modern.member.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private Authentication authentication;

    @Test
    void authenticateUser_Success() {
        LoginRequest loginRequest = new LoginRequest("testuser", "password");

        UserDetailsImpl userDetails = new UserDetailsImpl(
                "1", "testuser", "test@email.com", "password", false, 1,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("mock-jwt-token");

        JwtResponse response = authService.authenticateUser(loginRequest);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.token());
        assertEquals("testuser", response.userName());
        assertEquals("ROLE_USER", response.role());
        assertFalse(response.isPasswordTemporary());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void registerUser_Success() {
        SignupRequest request = new SignupRequest(
                "newuser", "ROLE_USER", "Test User", "test@email.com", "9876543210"
        );

        when(memberRepository.existsByEmail(request.email())).thenReturn(false);
        when(memberRepository.existsByUserName(request.userName())).thenReturn(false);
        when(memberRepository.existsByPhoneNumber(request.phoneNumber())).thenReturn(false);
        when(encoder.encode("Welcome@123")).thenReturn("encodedPassword");

        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member m = invocation.getArgument(0);
            m.setId("100");
            m.setCreatedAt(LocalDateTime.now());
            m.setUpdatedAt(LocalDateTime.now());
            return m;
        });

        MemberResponse response = authService.registerUser(request);

        assertNotNull(response);
        assertEquals("100", response.id());
        assertEquals("newuser", response.userName());
        assertEquals(Role.ROLE_USER, response.role());

        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void registerUser_Fail_EmailExists() {
        SignupRequest request = new SignupRequest(
                "newuser", "ROLE_USER", "Test User", "exists@email.com", "9876543210"
        );

        when(memberRepository.existsByEmail(request.email())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.registerUser(request));
        assertEquals(ErrorCode.EMAIL_ALREADY_IN_USE, ex.getErrorCode());

        verify(memberRepository, never()).save(any());
    }

    @Test
    void registerUser_Fail_UsernameExists() {
        SignupRequest request = new SignupRequest(
                "exists", "ROLE_USER", "Test User", "test@email.com", "9876543210"
        );

        when(memberRepository.existsByEmail(request.email())).thenReturn(false);
        when(memberRepository.existsByUserName(request.userName())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.registerUser(request));
        assertEquals(ErrorCode.USERNAME_ALREADY_EXISTS, ex.getErrorCode());

        verify(memberRepository, never()).save(any());
    }

    @Test
    void registerUser_Fail_PhoneExists() {
        SignupRequest request = new SignupRequest(
                "newuser", "ROLE_USER", "Test User", "test@email.com", "9999999999"
        );

        when(memberRepository.existsByEmail(request.email())).thenReturn(false);
        when(memberRepository.existsByUserName(request.userName())).thenReturn(false);
        when(memberRepository.existsByPhoneNumber(request.phoneNumber())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.registerUser(request));
        assertEquals(ErrorCode.PHONE_NUMBER_IN_USE, ex.getErrorCode());

        verify(memberRepository, never()).save(any());
    }

    @Test
    void registerUser_Fail_InvalidRole() {
        SignupRequest request = new SignupRequest(
                "newuser", "INVALID_ROLE", "Test User", "test@email.com", "9876543210"
        );

        when(memberRepository.existsByEmail(request.email())).thenReturn(false);
        when(memberRepository.existsByUserName(request.userName())).thenReturn(false);
        when(memberRepository.existsByPhoneNumber(request.phoneNumber())).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.registerUser(request));
        assertEquals(ErrorCode.ROLE_NOT_FOUND, ex.getErrorCode());

        verify(memberRepository, never()).save(any());
    }

    @Test
    void registerUser_Fail_InvalidPhoneFormat() {
        SignupRequest request = new SignupRequest(
                "newuser", "ROLE_USER", "Test User", "test@email.com", "1234567890"
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.registerUser(request));
        assertEquals(ErrorCode.INVALID_PHONE_FORMAT, ex.getErrorCode());

        verify(memberRepository, never()).save(any());
    }
}