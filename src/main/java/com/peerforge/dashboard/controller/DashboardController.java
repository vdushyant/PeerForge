package com.peerforge.dashboard.controller;

import com.peerforge.dashboard.dto.MentorDashboardResponse;
import com.peerforge.dashboard.dto.PlatformAnalyticsResponse;
import com.peerforge.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/api/v1/dashboard/mentor")
    public MentorDashboardResponse getMentorDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        return dashboardService.getMentorDashboard(userDetails.getUsername());
    }

    @GetMapping("/api/v1/admin/analytics")
    public PlatformAnalyticsResponse getPlatformAnalytics() {
        return dashboardService.getPlatformAnalytics();
    }
}
