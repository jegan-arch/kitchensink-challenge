package com.modern.member.service.impl;

import com.modern.member.dto.ChangePasswordRequest;
import com.modern.member.dto.MemberRequest;
import com.modern.member.dto.MemberResponse;
import com.modern.member.exception.BusinessException;
import com.modern.member.exception.ErrorCode;
import com.modern.member.model.Member;
import com.modern.member.model.Role;
import com.modern.member.repository.MemberRepository;
import com.modern.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<MemberResponse> getAllMembers() {
        UserDetailsImpl currentUser = getCurrentUserDetails();
        boolean isAdmin = isRole(currentUser, Role.ROLE_ADMIN);

        List<Member> members = isAdmin
                ? memberRepository.findAll()
                : memberRepository.findByRoleNot(Role.ROLE_ADMIN);

        return members.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public MemberResponse getMemberById(String id) {
        Member member = findMemberOrThrow(id);
        validateViewAccess(member);
        return mapToResponse(member);
    }

    @Override
    public MemberResponse getMyProfile() {
        String currentUserId = getCurrentUserDetails().getId();
        return getMemberById(currentUserId);
    }

    @Override
    @Transactional
    public MemberResponse updateMember(String id, MemberRequest request) {
        Member member = findMemberOrThrow(id);
        validateModifyAccess(member);

        if (!member.getEmail().equals(request.email()) && memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_IN_USE);
        }

        if (memberRepository.existsByPhoneNumber(request.phoneNumber())
                && !member.getPhoneNumber().equals(request.phoneNumber())) {
            throw new BusinessException(ErrorCode.PHONE_NUMBER_IN_USE);
        }

        if (!request.phoneNumber().matches("^[6-9]\\d{9}$")) {
            throw new BusinessException(ErrorCode.INVALID_PHONE_FORMAT);
        }

        if (request.role() != null) {
            Role newRole = resolveRole(request.role());

            if (member.getRole() != newRole) {
                UserDetailsImpl currentUser = getCurrentUserDetails();
                if (!isRole(currentUser, Role.ROLE_ADMIN)) {
                    throw new BusinessException(ErrorCode.ACCESS_DENIED);
                }

                if (member.getRole() == Role.ROLE_ADMIN && newRole != Role.ROLE_ADMIN) {
                    long adminCount = memberRepository.countByRole(Role.ROLE_ADMIN);
                    if (adminCount <= 1) {
                        throw new BusinessException(ErrorCode.CANNOT_DOWNGRADE_LAST_ADMIN);
                    }
                }

                member.setRole(newRole);
                member.setTokenVersion(member.getTokenVersion() + 1);
            }
        }

        member.setName(request.name());
        member.setEmail(request.email());
        member.setPhoneNumber(request.phoneNumber());

        Member updatedMember = memberRepository.save(member);
        return mapToResponse(updatedMember);
    }

    @Transactional
    @Override
    public void changePassword(String id, ChangePasswordRequest request) {
        Member member = findMemberOrThrow(id);

        if (!getCurrentUserDetails().getId().equals(member.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if (!passwordEncoder.matches(request.oldPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        member.setPassword(passwordEncoder.encode(request.newPassword()));
        member.setTokenVersion(member.getTokenVersion() + 1);
        member.setPasswordTemporary(false);

        memberRepository.save(member);
    }

    @Override
    public void deleteMember(String id) {
        Member member = findMemberOrThrow(id);
        validateModifyAccess(member);

        if (member.getRole() == Role.ROLE_ADMIN) {
            long adminCount = memberRepository.countByRole(Role.ROLE_ADMIN);
            if (adminCount <= 1) {
                throw new BusinessException(ErrorCode.CANNOT_DELETE_LAST_ADMIN);
            }
        }
        memberRepository.delete(member);
    }

    @Override
    public void updateMemberRole(String id, String roleName) {
        Member member = findMemberOrThrow(id);
        Role role = resolveRole(roleName);

        member.setRole(role);
        member.setTokenVersion(member.getTokenVersion() + 1);

        memberRepository.save(member);
    }

    private void validateViewAccess(Member targetMember) {
        UserDetailsImpl currentUser = getCurrentUserDetails();

        if (isRole(currentUser, Role.ROLE_ADMIN)) return;

        if (!currentUser.getId().equals(targetMember.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void validateModifyAccess(Member targetMember) {
        UserDetailsImpl currentUser = getCurrentUserDetails();

        if (isRole(currentUser, Role.ROLE_ADMIN)) return;

        if (!currentUser.getId().equals(targetMember.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    private Member findMemberOrThrow(String id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private UserDetailsImpl getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl userDetails) {
            return userDetails;
        }
        throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
    }

    private boolean isRole(UserDetailsImpl user, Role role) {
        return user.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), role.name()));
    }

    private Role resolveRole(String roleStr) {
        if (roleStr == null) return Role.ROLE_USER;
        return switch (roleStr.toUpperCase()) {
            case "ROLE_ADMIN", "ADMIN" -> Role.ROLE_ADMIN;
            default -> Role.ROLE_USER;
        };
    }

    private MemberResponse mapToResponse(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getUserName(),
                member.getEmail(),
                member.getPhoneNumber(),
                member.getRole(),
                member.getCreatedAt(),
                member.getUpdatedAt(),
                member.getCreatedBy() == null ? "System" : member.getCreatedBy(),
                member.getUpdatedBy() == null ? "System" : member.getUpdatedBy()
        );
    }
}