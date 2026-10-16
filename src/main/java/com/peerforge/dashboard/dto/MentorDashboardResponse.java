package com.peerforge.dashboard.dto;

import java.math.BigDecimal;

public record MentorDashboardResponse(
        long completedSessions,
        long pendingBookings,
        long confirmedSessions,
        long upcomingSessions,
        long approvedReviewCount,
        BigDecimal averageRating,
        BigDecimal grossSuccessfulPayments
) { }
