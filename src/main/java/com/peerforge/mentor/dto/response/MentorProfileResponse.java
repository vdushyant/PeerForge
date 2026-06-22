package com.peerforge.mentor.dto.response;

import com.peerforge.mentor.entity.ApprovalStatus;

import java.math.BigDecimal;

public record MentorProfileResponse(

        Long id,

        String about,

        BigDecimal hourlyRate,

        ApprovalStatus approvalStatus,

        Integer sessionsCompleted,

        BigDecimal averageRating
) {
}