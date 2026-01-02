package com.modern.kitchensink.dto;

import java.time.LocalDateTime;

public record MemberResponse(
        String id,
        String name,
        String email,
        String phoneNumber,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}