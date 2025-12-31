package com.modern.kitchensink.service;

import com.modern.kitchensink.dto.MemberRequest;
import com.modern.kitchensink.dto.MemberResponse;
import java.util.List;

public interface MemberService {
    List<MemberResponse> getAllMembers();
    MemberResponse registerMember(MemberRequest request);
    void deleteMember(String id);
}