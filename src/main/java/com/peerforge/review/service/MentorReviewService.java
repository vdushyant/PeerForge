package com.peerforge.review.service;

import com.peerforge.review.dto.request.CreateReviewRequest;
import com.peerforge.review.dto.response.ReviewEligibilityResponse;
import com.peerforge.review.dto.response.ReviewResponse;
import com.peerforge.review.entity.ReviewStatus;

import java.util.List;

public interface MentorReviewService {
    ReviewEligibilityResponse getEligibility(Long sessionId, String email);

    ReviewResponse submitReview(Long sessionId, CreateReviewRequest request, String email);

    List<ReviewResponse> getPublishedReviews(Long mentorId);

    List<ReviewResponse> getReviewsForModeration(ReviewStatus status);

    ReviewResponse approveReview(Long reviewId);

    ReviewResponse rejectReview(Long reviewId, String reason);
}
