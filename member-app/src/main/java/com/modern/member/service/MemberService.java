package com.modern.member.service;

import com.modern.member.dto.ChangePasswordRequest;
import com.modern.member.dto.MemberRequest;
import com.modern.member.dto.MemberResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MemberService {
    List<MemberResponse> getAllMembers();
    MemberResponse getMemberById(String id);
    MemberResponse getMyProfile();
    MemberResponse updateMember(String id, MemberRequest request);

    @Transactional
    void changePassword(String id, ChangePasswordRequest request);

    void deleteMember(String id);
    void updateMemberRole(String id, String roleName);
}