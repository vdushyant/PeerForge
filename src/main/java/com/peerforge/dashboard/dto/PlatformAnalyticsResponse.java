package com.peerforge.dashboard.dto;

import java.math.BigDecimal;

public record PlatformAnalyticsResponse(
        long totalUsers,
        long approvedMentors,
        long pendingMentorApplications,
        long totalSessions,
        long bookingsLast30Days,
        long pendingSessions,
        long completedSessions,
        long cancelledSessions,
        long pendingReviews,
        BigDecimal grossSuccessfulPayments
) { }
