package com.peerforge.admin.controller;

import com.peerforge.mentor.dto.response.MentorProfileResponse;
import com.peerforge.mentor.service.MentorService;
import com.peerforge.review.dto.request.RejectReviewRequest;
import com.peerforge.review.dto.response.ReviewResponse;
import com.peerforge.review.entity.ReviewStatus;
import com.peerforge.review.service.MentorReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MentorService mentorService;
    private final MentorReviewService reviewService;

    @PatchMapping(
            "/mentors/{mentorId}/approve"
    )

    public MentorProfileResponse approveMentor(

            @PathVariable
            Long mentorId
    ) {

        return mentorService
                .approveMentor(
                        mentorId
                );
    }

    @GetMapping("/reviews")
    public List<ReviewResponse> getReviewsForModeration(
            @RequestParam(defaultValue = "PENDING") ReviewStatus status
    ) {
        return reviewService.getReviewsForModeration(status);
    }

    @PatchMapping("/reviews/{reviewId}/approve")
    public ReviewResponse approveReview(@PathVariable Long reviewId) {
        return reviewService.approveReview(reviewId);
    }

    @PatchMapping("/reviews/{reviewId}/reject")
    public ReviewResponse rejectReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody RejectReviewRequest request
    ) {
        return reviewService.rejectReview(reviewId, request.reason());
    }

}
