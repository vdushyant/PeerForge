package com.peerforge.landing.controller;

import com.peerforge.landing.dto.response.FeaturedReviewResponse;
import com.peerforge.landing.dto.response.LandingStatsResponse;
import com.peerforge.landing.service.LandingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public")
public class LandingController {

    private final LandingService landingService;

    @GetMapping("/landing-stats")
    public LandingStatsResponse getStats() {
        return landingService.getStats();
    }

    @GetMapping("/featured-reviews")
    public List<FeaturedReviewResponse> getFeaturedReviews() {
        return landingService.getFeaturedReviews();
    }
}
