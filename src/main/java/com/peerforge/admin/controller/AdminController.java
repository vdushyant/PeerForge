package com.peerforge.admin.controller;

import com.peerforge.admin.dto.AdminSessionResponse;
import com.peerforge.admin.dto.AdminUserResponse;
import com.peerforge.admin.service.AdminDirectoryService;
import com.peerforge.mentor.dto.request.RejectMentorApplicationRequest;
import com.peerforge.mentor.dto.response.MentorApplicationResponse;
import com.peerforge.mentor.entity.ApprovalStatus;
import com.peerforge.mentor.dto.response.MentorProfileResponse;
import com.peerforge.mentor.service.MentorService;
import com.peerforge.review.dto.request.RejectReviewRequest;
import com.peerforge.review.dto.response.ReviewResponse;
import com.peerforge.review.entity.ReviewStatus;
import com.peerforge.review.service.MentorReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MentorService mentorService;
    private final MentorReviewService reviewService;
    private final AdminDirectoryService directoryService;

    @GetMapping("/users")
    public Page<AdminUserResponse> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return directoryService.getUsers(pageRequest(page, size));
    }

    @GetMapping("/sessions")
    public Page<AdminSessionResponse> getSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return directoryService.getSessions(pageRequest(page, size));
    }

    @GetMapping("/mentors")
    public Page<MentorApplicationResponse> getMentorApplications(
            @RequestParam(defaultValue = "PENDING") ApprovalStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return mentorService.getMentorApplications(status, pageRequest(page, size));
    }

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

    @PatchMapping("/mentors/{mentorId}/reject")
    public MentorProfileResponse rejectMentor(
            @PathVariable Long mentorId,
            @Valid @RequestBody RejectMentorApplicationRequest request
    ) {
        return mentorService.rejectMentor(mentorId, request.reason());
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

    private Pageable pageRequest(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

}
