package com.peerforge.mentor.dto.response;

import com.peerforge.mentor.entity.ApprovalStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MentorApplicationResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String about,
        BigDecimal hourlyRate,
        ApprovalStatus approvalStatus,
        String rejectionReason,
        LocalDateTime submittedAt
) { }
