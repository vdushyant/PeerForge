package com.peerforge.review.controller;

import com.peerforge.review.dto.request.CreateReviewRequest;
import com.peerforge.review.dto.response.ReviewEligibilityResponse;
import com.peerforge.review.dto.response.ReviewResponse;
import com.peerforge.review.service.MentorReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MentorReviewController {

    private final MentorReviewService reviewService;

    @GetMapping("/api/v1/sessions/{sessionId}/review")
    public ReviewEligibilityResponse getEligibility(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return reviewService.getEligibility(sessionId, userDetails.getUsername());
    }

    @PostMapping("/api/v1/sessions/{sessionId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse submitReview(
            @PathVariable Long sessionId,
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return reviewService.submitReview(sessionId, request, userDetails.getUsername());
    }

    @GetMapping("/api/v1/mentors/{mentorId}/reviews")
    public List<ReviewResponse> getPublishedReviews(@PathVariable Long mentorId) {
        return reviewService.getPublishedReviews(mentorId);
    }
}
