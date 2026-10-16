package com.peerforge.dashboard.service;

import com.peerforge.dashboard.dto.MentorDashboardResponse;
import com.peerforge.dashboard.dto.PlatformAnalyticsResponse;

public interface DashboardService {

    MentorDashboardResponse getMentorDashboard(String email);

    PlatformAnalyticsResponse getPlatformAnalytics();
}
