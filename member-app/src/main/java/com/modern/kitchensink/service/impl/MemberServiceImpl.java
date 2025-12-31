package com.modern.kitchensink.service.impl;

import com.modern.kitchensink.dto.MemberRequest;
import com.modern.kitchensink.dto.MemberResponse;
import com.modern.kitchensink.exception.ErrorCode;
import com.modern.kitchensink.exception.KitchensinkException;
import com.modern.kitchensink.model.Member;
import com.modern.kitchensink.repository.MemberRepository;
import com.modern.kitchensink.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    private static final Pattern INDIAN_PHONE_PATTERN =
            Pattern.compile("^(\\+91|91)?[\\-\\s]?[6-9]\\d{9}$");

    @Override
    public List<MemberResponse> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public MemberResponse registerMember(MemberRequest request) {
        validateIndianMobileNumber(request.phoneNumber());
        validateEmailUniqueness(request.email());

        Member member = createMemberEntity(request);
        Member saved = memberRepository.save(member);

        return mapToResponse(saved);
    }

    @Override
    public void deleteMember(String id) {
        if (!memberRepository.existsById(id)) {
            throw new KitchensinkException(ErrorCode.MEMBER_NOT_FOUND);
        }
        memberRepository.deleteById(id);
    }

    private void validateIndianMobileNumber(String phoneNumber) {
        if (phoneNumber == null || !INDIAN_PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new KitchensinkException(ErrorCode.INVALID_PHONE_FORMAT);
        }
    }

    private void validateEmailUniqueness(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new KitchensinkException(ErrorCode.EMAIL_ALREADY_IN_USE);
        }
    }

    private Member createMemberEntity(MemberRequest request) {
        Member member = new Member();
        member.setName(request.name());
        member.setEmail(request.email());
        member.setPhoneNumber(request.phoneNumber());
        return member;
    }

    private MemberResponse mapToResponse(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getPhoneNumber()
        );
    }
}