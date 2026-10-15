package com.peerforge.review.service.impl;

import com.peerforge.common.exception.DuplicateResourceException;
import com.peerforge.common.exception.InvalidReviewStateException;
import com.peerforge.common.exception.ResourceNotFoundException;
import com.peerforge.mentor.entity.ApprovalStatus;
import com.peerforge.mentor.entity.MentorProfile;
import com.peerforge.mentor.repository.MentorProfileRepository;
import com.peerforge.review.dto.request.CreateReviewRequest;
import com.peerforge.review.dto.response.ReviewEligibilityResponse;
import com.peerforge.review.dto.response.ReviewResponse;
import com.peerforge.review.entity.MentorReview;
import com.peerforge.review.entity.ReviewStatus;
import com.peerforge.review.repository.MentorReviewRepository;
import com.peerforge.review.service.MentorReviewService;
import com.peerforge.session.entity.Session;
import com.peerforge.session.entity.SessionStatus;
import com.peerforge.session.repository.SessionRepository;
import com.peerforge.user.entity.User;
import com.peerforge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MentorReviewServiceImpl implements MentorReviewService {

    private final MentorReviewRepository reviewRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final MentorProfileRepository mentorProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public ReviewEligibilityResponse getEligibility(Long sessionId, String email) {
        Session session = getSession(sessionId);
        User client = getCurrentUser(email);
        requireSessionClient(session, client);

        var existing = reviewRepository.findBySession_Id(sessionId);
        if (existing.isPresent()) {
            MentorReview review = existing.get();
            String reason = switch (review.getStatus()) {
                case PENDING -> "Your review is awaiting moderation.";
                case APPROVED -> "You have already reviewed this session.";
                case REJECTED -> "Your review was rejected. " + review.getModerationNote();
            };
            return new ReviewEligibilityResponse(false, reason, toResponse(review));
        }

        if (session.getStatus() != SessionStatus.COMPLETED) {
            return new ReviewEligibilityResponse(
                    false,
                    "A review can be submitted after the session is completed.",
                    null
            );
        }

        return new ReviewEligibilityResponse(true, null, null);
    }

    @Override
    public ReviewResponse submitReview(Long sessionId, CreateReviewRequest request, String email) {
        Session session = getSession(sessionId);
        User client = getCurrentUser(email);
        requireSessionClient(session, client);

        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new InvalidReviewStateException(
                    "Only completed sessions can be reviewed"
            );
        }

        if (session.getMentor().getUser().getId().equals(client.getId())) {
            throw new AccessDeniedException("Mentors cannot review their own sessions");
        }

        if (reviewRepository.findBySession_Id(sessionId).isPresent()) {
            throw new DuplicateResourceException("This session already has a review");
        }

        MentorReview review = MentorReview.builder()
                .session(session)
                .mentor(session.getMentor())
                .client(client)
                .rating(request.rating())
                .comment(normalizeComment(request.comment()))
                .status(ReviewStatus.PENDING)
                .build();

        try {
            return toResponse(reviewRepository.saveAndFlush(review));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException("This session already has a review");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getPublishedReviews(Long mentorId) {
        MentorProfile mentor = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));
        if (mentor.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new ResourceNotFoundException("Mentor not found");
        }

        return reviewRepository
                .findByMentor_IdAndStatusOrderByCreatedAtDesc(mentorId, ReviewStatus.APPROVED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForModeration(ReviewStatus status) {
        return reviewRepository.findAllByStatusOrderByCreatedAtAsc(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ReviewResponse approveReview(Long reviewId) {
        MentorReview review = getReviewForUpdate(reviewId);
        if (review.getStatus() == ReviewStatus.APPROVED) {
            return toResponse(review);
        }
        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new InvalidReviewStateException("Only pending reviews can be approved");
        }

        MentorProfile mentor = mentorProfileRepository
                .findByIdForUpdate(review.getMentor().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));

        review.setStatus(ReviewStatus.APPROVED);
        review.setModerationNote(null);
        reviewRepository.saveAndFlush(review);

        long reviewCount = reviewRepository.countByMentor_IdAndStatus(
                mentor.getId(), ReviewStatus.APPROVED
        );
        Double average = reviewRepository.findAverageRating(
                mentor.getId(), ReviewStatus.APPROVED
        );
        mentor.setReviewCount(Math.toIntExact(reviewCount));
        mentor.setAverageRating(average == null
                ? BigDecimal.ZERO.setScale(1)
                : BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP));

        return toResponse(review);
    }

    @Override
    public ReviewResponse rejectReview(Long reviewId, String reason) {
        MentorReview review = getReviewForUpdate(reviewId);
        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new InvalidReviewStateException("Only pending reviews can be rejected");
        }

        review.setStatus(ReviewStatus.REJECTED);
        review.setModerationNote(reason.trim());
        return toResponse(reviewRepository.save(review));
    }

    private Session getSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
    }

    private User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private MentorReview getReviewForUpdate(Long reviewId) {
        return reviewRepository.findByIdForUpdate(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
    }

    private void requireSessionClient(Session session, User client) {
        if (!session.getClient().getId().equals(client.getId())) {
            throw new AccessDeniedException("Only the client who booked the session can review it");
        }
    }

    private String normalizeComment(String comment) {
        if (comment == null || comment.isBlank()) {
            return null;
        }
        return comment.trim();
    }

    private ReviewResponse toResponse(MentorReview review) {
        User reviewer = review.getClient();
        String lastInitial = reviewer.getLastName() == null || reviewer.getLastName().isBlank()
                ? ""
                : " " + reviewer.getLastName().charAt(0) + ".";
        return new ReviewResponse(
                review.getId(),
                review.getSession().getId(),
                review.getMentor().getId(),
                review.getMentor().getUser().getFirstName() + " "
                        + review.getMentor().getUser().getLastName(),
                reviewer.getFirstName() + lastInitial,
                review.getRating(),
                review.getComment(),
                review.getStatus(),
                review.getModerationNote(),
                review.getCreatedAt()
        );
    }
}
