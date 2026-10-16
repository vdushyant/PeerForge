package com.peerforge.landing.dto.response;

import java.time.LocalDateTime;

public record FeaturedReviewResponse(
        Long id,
        String mentorName,
        String reviewerName,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {
}
