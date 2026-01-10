package com.modern.member.dto;

import java.time.LocalDateTime;
import com.modern.member.model.Role;

public record MemberResponse(
        String id,
        String name,
        String userName,
        String email,
        String phoneNumber,
        Role role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}