package com.peerforge.landing.dto.response;

import java.math.BigDecimal;

public record LandingStatsResponse(
        long mentorCount,
        long completedSessionCount,
        long skillCount,
        BigDecimal averageRating
) {
}
