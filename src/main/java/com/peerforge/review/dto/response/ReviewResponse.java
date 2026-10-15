package com.peerforge.review.dto.response;

import com.peerforge.review.entity.ReviewStatus;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long sessionId,
        Long mentorId,
        String mentorName,
        String reviewerName,
        Integer rating,
        String comment,
        ReviewStatus status,
        String moderationNote,
        LocalDateTime createdAt
) {
}
