package com.modern.member.service;

import com.modern.member.dto.ChangePasswordRequest;
import com.modern.member.dto.MemberRequest;
import com.modern.member.dto.MemberResponse;
import com.modern.member.exception.BusinessException;
import com.modern.member.exception.ErrorCode;
import com.modern.member.model.Member;
import com.modern.member.model.Role;
import com.modern.member.repository.MemberRepository;
import com.modern.member.service.impl.MemberServiceImpl;
import com.modern.member.service.impl.UserDetailsImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member adminMember;
    private Member userMember;
    private Member otherUserMember;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);

        adminMember = new Member();
        adminMember.setId("admin-id");
        adminMember.setUserName("admin");
        adminMember.setRole(Role.ROLE_ADMIN);
        adminMember.setEmail("admin@test.com");
        adminMember.setTokenVersion(1);

        userMember = new Member();
        userMember.setId("user-id");
        userMember.setUserName("user");
        userMember.setRole(Role.ROLE_USER);
        userMember.setEmail("user@test.com");
        userMember.setPhoneNumber("1234567890");
        userMember.setTokenVersion(1);

        otherUserMember = new Member();
        otherUserMember.setId("other-id");
        otherUserMember.setUserName("other");
        otherUserMember.setRole(Role.ROLE_USER);
        otherUserMember.setEmail("other@test.com");
        otherUserMember.setPhoneNumber("0987654321");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(Member member) {
        UserDetailsImpl userDetails = new UserDetailsImpl(
                member.getId(),
                member.getUserName(),
                member.getEmail(),
                "password",
                false,
                member.getTokenVersion(),
                Collections.singletonList(new SimpleGrantedAuthority(member.getRole().name()))
        );

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        lenient().when(authentication.getAuthorities()).thenAnswer(i -> userDetails.getAuthorities());
    }

    @Test
    void getAllMembers_AdminCanViewAll() {
        mockSecurityContext(adminMember);
        when(memberRepository.findAll()).thenReturn(List.of(adminMember, userMember));

        List<MemberResponse> result = memberService.getAllMembers();

        assertEquals(2, result.size());
        verify(memberRepository).findAll();
    }

    @Test
    void getAllMembers_UserCannotViewAdmins() {
        mockSecurityContext(userMember);
        when(memberRepository.findByRoleNot(Role.ROLE_ADMIN)).thenReturn(List.of(userMember));

        List<MemberResponse> result = memberService.getAllMembers();

        assertEquals(1, result.size());
        assertEquals("user", result.get(0).userName());
        verify(memberRepository).findByRoleNot(Role.ROLE_ADMIN);
    }

    @Test
    void getMemberById_Success_SameUser() {
        mockSecurityContext(userMember);
        when(memberRepository.findById("user-id")).thenReturn(Optional.of(userMember));

        MemberResponse response = memberService.getMemberById("user-id");
        assertEquals("user", response.userName());
    }

    @Test
    void getMemberById_Success_AdminViewingUser() {
        mockSecurityContext(adminMember);
        when(memberRepository.findById("user-id")).thenReturn(Optional.of(userMember));

        MemberResponse response = memberService.getMemberById("user-id");
        assertEquals("user", response.userName());
    }

    @Test
    void getMemberById_Fail_UserViewingOtherUser() {
        mockSecurityContext(userMember);
        when(memberRepository.findById("other-id")).thenReturn(Optional.of(otherUserMember));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                memberService.getMemberById("other-id")
        );
        assertEquals(ErrorCode.ACCESS_DENIED, ex.getErrorCode());
    }

    @Test
    void getMemberById_Fail_NotFound() {
        mockSecurityContext(adminMember);
        when(memberRepository.findById("non-existent")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                memberService.getMemberById("non-existent")
        );
        assertEquals(ErrorCode.MEMBER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getMyProfile_Success() {
        mockSecurityContext(userMember);
        when(memberRepository.findById("user-id")).thenReturn(Optional.of(userMember));

        MemberResponse response = memberService.getMyProfile();
        assertEquals("user", response.userName());
    }

    @Test
    void updateMember_Success() {
        mockSecurityContext(adminMember);
        when(memberRepository.findById("user-id")).thenReturn(Optional.of(userMember));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MemberRequest request = new MemberRequest("New Name", "user@test.com", "9876543210", "ROLE_USER");
        MemberResponse response = memberService.updateMember("user-id", request);

        assertEquals("New Name", response.name());
        verify(memberRepository).save(userMember);
    }

    @Test
    void updateMember_Fail_EmailAlreadyExists() {
        mockSecurityContext(userMember);
        when(memberRepository.findById("user-id")).thenReturn(Optional.of(userMember));
        when(memberRepository.existsByEmail("other@test.com")).thenReturn(true);

        MemberRequest request = new MemberRequest("Name", "other@test.com", "1234567890", "ROLE_USER");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                memberService.updateMember("user-id", request)
        );
        assertEquals(ErrorCode.EMAIL_ALREADY_IN_USE, ex.getErrorCode());
    }

    @Test
    void updateMember_Fail_PhoneAlreadyExists() {
        mockSecurityContext(userMember);
        when(memberRepository.findById("user-id")).thenReturn(Optional.of(userMember));
        when(memberRepository.existsByPhoneNumber("0987654321")).thenReturn(true);

        MemberRequest request = new MemberRequest("Name", "user@test.com", "0987654321", "ROLE_USER");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                memberService.updateMember("user-id", request)
        );
        assertEquals(ErrorCode.PHONE_NUMBER_IN_USE, ex.getErrorCode());
    }

    @Test
    void updateMember_Fail_UserModifyingOtherUser() {
        mockSecurityContext(userMember);
        when(memberRepository.findById("other-id")).thenReturn(Optional.of(otherUserMember));

        MemberRequest request = new MemberRequest("Name", "other@test.com", "0000000000", "ROLE_USER");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                memberService.updateMember("other-id", request)
        );
        assertEquals(ErrorCode.ACCESS_DENIED, ex.getErrorCode());
    }

    @Test
    void updateMember_RoleChange_IncrementsTokenVersion() {
        mockSecurityContext(adminMember);
        when(memberRepository.findById("user-id")).thenReturn(Optional.of(userMember));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MemberRequest request = new MemberRequest("User", "user@test.com", "9876543210", "ROLE_ADMIN");

        int oldVersion = userMember.getTokenVersion();
        memberService.updateMember("user-id", request);

        assertEquals(Role.ROLE_ADMIN, userMember.getRole());
        assertEquals(oldVersion + 1, userMember.getTokenVersion());
    }

    @Test
    void updateMember_CannotDowngradeLastAdmin() {
        mockSecurityContext(adminMember);
        when(memberRepository.findById("admin-id")).thenReturn(Optional.of(adminMember));
        when(memberRepository.countByRole(Role.ROLE_ADMIN)).thenReturn(1L);

        MemberRequest request = new MemberRequest("Admin", "admin@test.com", "9876543210", "ROLE_USER");

        BusinessException exception = assertThrows(BusinessException.class, () ->
                memberService.updateMember("admin-id", request)
        );

        assertEquals(ErrorCode.CANNOT_DOWNGRADE_LAST_ADMIN, exception.getErrorCode());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void updateMember_LastAdminCanUpdateDetailsWithoutDowngrade() {
        mockSecurityContext(adminMember);
        when(memberRepository.findById("admin-id")).thenReturn(Optional.of(adminMember));
        when(memberRepository.save(any(Member.class))).thenAnswer(i -> i.getArgument(0));

        // request to stay ADMIN
        MemberRequest request = new MemberRequest("New Name", "admin@test.com", "9876543210", "ROLE_ADMIN");

        MemberResponse response = memberService.updateMember("admin-id", request);

        assertEquals("New Name", response.name());
        assertEquals(Role.ROLE_ADMIN, response.role());
    }

    @Test
    void deleteMember_CannotDeleteLastAdmin() {
        mockSecurityContext(adminMember);
        when(memberRepository.findById("admin-id")).thenReturn(Optional.of(adminMember));
        when(memberRepository.countByRole(Role.ROLE_ADMIN)).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                memberService.deleteMember("admin-id")
        );

        assertEquals(ErrorCode.CANNOT_DELETE_LAST_ADMIN, exception.getErrorCode());
        verify(memberRepository, never()).delete(any());
    }

    @Test
    void deleteMember_Fail_UserDeletingOtherUser() {
        mockSecurityContext(userMember);
        when(memberRepository.findById("other-id")).thenReturn(Optional.of(otherUserMember));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                memberService.deleteMember("other-id")
        );
        assertEquals(ErrorCode.ACCESS_DENIED, ex.getErrorCode());
    }

    @Test
    void deleteMember_Success() {
        mockSecurityContext(adminMember);
        when(memberRepository.findById("user-id")).thenReturn(Optional.of(userMember));

        memberService.deleteMember("user-id");

        verify(memberRepository).delete(userMember);
    }

    @Test
    void updateMemberRole_Success() {
        mockSecurityContext(adminMember);
        when(memberRepository.findById("user-id")).thenReturn(Optional.of(userMember));

        int oldVersion = userMember.getTokenVersion();
        memberService.updateMemberRole("user-id", "ROLE_ADMIN");

        assertEquals(Role.ROLE_ADMIN, userMember.getRole());
        assertEquals(oldVersion + 1, userMember.getTokenVersion());
        verify(memberRepository).save(userMember);
    }

    @Test
    void changePassword_Success() {
        mockSecurityContext(userMember);
        userMember.setPassword("encodedOldPass");

        when(memberRepository.findById("user-id")).thenReturn(Optional.of(userMember));
        when(passwordEncoder.matches("oldPass", "encodedOldPass")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

        ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "newPass");
        int oldVersion = userMember.getTokenVersion();

        memberService.changePassword("user-id", request);

        assertEquals("encodedNewPass", userMember.getPassword());
        assertEquals(oldVersion + 1, userMember.getTokenVersion());
    }

    @Test
    void changePassword_Fail_AccessDeniedDifferentUser() {
        mockSecurityContext(userMember);
        // Trying to change someone else's password
        when(memberRepository.findById("other-id")).thenReturn(Optional.of(otherUserMember));

        ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "newPass");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                memberService.changePassword("other-id", request)
        );
        assertEquals(ErrorCode.ACCESS_DENIED, ex.getErrorCode());
    }

    @Test
    void changePassword_Fail_WrongOldPassword() {
        mockSecurityContext(userMember);
        userMember.setPassword("encodedOldPass");

        when(memberRepository.findById("user-id")).thenReturn(Optional.of(userMember));
        when(passwordEncoder.matches("wrongPass", "encodedOldPass")).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest("wrongPass", "newPass");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                memberService.changePassword("user-id", request)
        );
        assertEquals(ErrorCode.ACCESS_DENIED, ex.getErrorCode());
    }

    @Test
    void getCurrentUserDetails_Fail_NoAuthentication() {
        when(securityContext.getAuthentication()).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                memberService.getAllMembers()
        );
        assertEquals(ErrorCode.AUTHENTICATION_FAILED, ex.getErrorCode());
    }

    @Test
    void updateMember_Fail_InvalidPhoneFormat() {
        mockSecurityContext(userMember);
        when(memberRepository.findById("user-id")).thenReturn(Optional.of(userMember));

        MemberRequest request = new MemberRequest("Name", "user@test.com", "123", "ROLE_USER");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                memberService.updateMember("user-id", request)
        );
        assertEquals(ErrorCode.INVALID_PHONE_FORMAT, ex.getErrorCode());
    }

    @Test
    void updateMember_Fail_UserCannotPromoteSelfToAdmin() {
        mockSecurityContext(userMember);

        when(memberRepository.findById("user-id")).thenReturn(Optional.of(userMember));

        MemberRequest request = new MemberRequest("Hacker", "user@test.com", "9876543210", "ROLE_ADMIN");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                memberService.updateMember("user-id", request)
        );

        assertEquals(ErrorCode.ACCESS_DENIED, ex.getErrorCode());

        verify(memberRepository, never()).save(any());
    }
}