package com.peerforge.landing.service;

import com.peerforge.landing.dto.response.FeaturedReviewResponse;
import com.peerforge.landing.dto.response.LandingStatsResponse;

import java.util.List;

public interface LandingService {
    LandingStatsResponse getStats();

    List<FeaturedReviewResponse> getFeaturedReviews();
}
