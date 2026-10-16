package com.peerforge.landing.service.impl;

import com.peerforge.landing.dto.response.FeaturedReviewResponse;
import com.peerforge.landing.dto.response.LandingStatsResponse;
import com.peerforge.landing.service.LandingService;
import com.peerforge.mentor.entity.ApprovalStatus;
import com.peerforge.mentor.repository.MentorProfileRepository;
import com.peerforge.review.entity.MentorReview;
import com.peerforge.review.entity.ReviewStatus;
import com.peerforge.review.repository.MentorReviewRepository;
import com.peerforge.session.entity.SessionStatus;
import com.peerforge.session.repository.SessionRepository;
import com.peerforge.skill.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LandingServiceImpl implements LandingService {

    private final MentorProfileRepository mentorRepository;
    private final SessionRepository sessionRepository;
    private final SkillRepository skillRepository;
    private final MentorReviewRepository reviewRepository;

    @Override
    public LandingStatsResponse getStats() {
        Double average = reviewRepository.findOverallAverageRating(ReviewStatus.APPROVED);
        BigDecimal averageRating = average == null
                ? BigDecimal.ZERO.setScale(1)
                : BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP);

        return new LandingStatsResponse(
                mentorRepository.countByApprovalStatus(ApprovalStatus.APPROVED),
                sessionRepository.countByStatus(SessionStatus.COMPLETED),
                skillRepository.count(),
                averageRating
        );
    }

    @Override
    public List<FeaturedReviewResponse> getFeaturedReviews() {
        return reviewRepository
                .findTop6ByStatusAndCommentIsNotNullOrderByCreatedAtDesc(ReviewStatus.APPROVED)
                .stream()
                .map(this::toFeaturedReview)
                .toList();
    }

    private FeaturedReviewResponse toFeaturedReview(MentorReview review) {
        String lastInitial = review.getClient().getLastName() == null
                || review.getClient().getLastName().isBlank()
                ? ""
                : " " + review.getClient().getLastName().charAt(0) + ".";
        return new FeaturedReviewResponse(
                review.getId(),
                review.getMentor().getUser().getFirstName() + " "
                        + review.getMentor().getUser().getLastName(),
                review.getClient().getFirstName() + lastInitial,
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
