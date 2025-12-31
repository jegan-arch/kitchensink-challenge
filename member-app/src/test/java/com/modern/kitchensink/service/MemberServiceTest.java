package com.modern.kitchensink.service;

import com.modern.kitchensink.dto.MemberRequest;
import com.modern.kitchensink.dto.MemberResponse;
import com.modern.kitchensink.exception.ErrorCode;
import com.modern.kitchensink.exception.KitchensinkException;
import com.modern.kitchensink.model.Member;
import com.modern.kitchensink.repository.MemberRepository;
import com.modern.kitchensink.service.impl.MemberServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberServiceImpl memberService;

    @Test
    void getAllMembers_ShouldReturnListOfResponses() {
        Member member = new Member();
        member.setId("1");
        member.setName("Test User");
        member.setEmail("test@example.com");
        member.setPhoneNumber("9876543210");

        when(memberRepository.findAll()).thenReturn(List.of(member));

        List<MemberResponse> result = memberService.getAllMembers();

        assertEquals(1, result.size());
        assertEquals("Test User", result.get(0).name());
        verify(memberRepository).findAll();
    }

    @Test
    void registerMember_WhenValidRequest_ShouldSaveAndReturnResponse() {
        MemberRequest request = new MemberRequest("John", "john@test.com", "9876543210");

        Member savedMember = new Member();
        savedMember.setId("100");
        savedMember.setName(request.name());
        savedMember.setEmail(request.email());
        savedMember.setPhoneNumber(request.phoneNumber());

        when(memberRepository.existsByEmail(request.email())).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        MemberResponse response = memberService.registerMember(request);

        assertNotNull(response.id());
        assertEquals("John", response.name());
        verify(memberRepository).save(any(Member.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1234567890",   // Starts with 1
            "98765",        // Too short
            "abcdefghij",   // Non-numeric
            "",             // Empty
            "   "           // Whitespace
    })
    void registerMember_WhenPhoneInvalid_ShouldThrowException(String invalidPhone) {
        MemberRequest request = new MemberRequest("John", "john@test.com", invalidPhone);

        KitchensinkException ex = assertThrows(KitchensinkException.class,
                () -> memberService.registerMember(request));

        assertEquals(ErrorCode.INVALID_PHONE_FORMAT, ex.getErrorCode());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void registerMember_WhenEmailExists_ShouldThrowException() {
        MemberRequest request = new MemberRequest("John", "john@test.com", "9876543210");

        when(memberRepository.existsByEmail(request.email())).thenReturn(true);

        KitchensinkException ex = assertThrows(KitchensinkException.class,
                () -> memberService.registerMember(request));

        assertEquals(ErrorCode.EMAIL_ALREADY_IN_USE, ex.getErrorCode());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void deleteMember_WhenIdExists_ShouldDelete() {
        String id = "123";
        when(memberRepository.existsById(id)).thenReturn(true);

        memberService.deleteMember(id);

        verify(memberRepository).deleteById(id);
    }

    @Test
    void deleteMember_WhenIdNotFound_ShouldThrowException() {
        String id = "123";
        when(memberRepository.existsById(id)).thenReturn(false);

        KitchensinkException ex = assertThrows(KitchensinkException.class,
                () -> memberService.deleteMember(id));

        assertEquals(ErrorCode.MEMBER_NOT_FOUND, ex.getErrorCode());
        verify(memberRepository, never()).deleteById(any());
    }
}