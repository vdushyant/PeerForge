package com.peerforge.dashboard.service.impl;

import com.peerforge.common.exception.ResourceNotFoundException;
import com.peerforge.dashboard.dto.MentorDashboardResponse;
import com.peerforge.dashboard.dto.PlatformAnalyticsResponse;
import com.peerforge.dashboard.service.DashboardService;
import com.peerforge.mentor.entity.ApprovalStatus;
import com.peerforge.mentor.entity.MentorProfile;
import com.peerforge.mentor.repository.MentorProfileRepository;
import com.peerforge.payment.entity.PaymentStatus;
import com.peerforge.payment.repository.PaymentRepository;
import com.peerforge.review.entity.ReviewStatus;
import com.peerforge.review.repository.MentorReviewRepository;
import com.peerforge.session.entity.SessionStatus;
import com.peerforge.session.repository.SessionRepository;
import com.peerforge.user.entity.User;
import com.peerforge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final SessionRepository sessionRepository;
    private final PaymentRepository paymentRepository;
    private final MentorReviewRepository reviewRepository;

    @Override
    public MentorDashboardResponse getMentorDashboard(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        MentorProfile mentor = mentorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Mentor profile not found"));

        long completed = sessionRepository.countByMentorIdAndStatus(
                mentor.getId(), SessionStatus.COMPLETED);
        long pending = sessionRepository.countByMentorIdAndStatus(
                mentor.getId(), SessionStatus.PENDING);
        long confirmed = sessionRepository.countByMentorIdAndStatus(
                mentor.getId(), SessionStatus.CONFIRMED);
        long upcoming = sessionRepository.countByMentorIdAndStatusAndStartDateTimeAfter(
                mentor.getId(), SessionStatus.CONFIRMED, LocalDateTime.now());
        BigDecimal grossPayments = paymentRepository.sumAmountByMentorAndStatus(
                mentor.getId(), PaymentStatus.SUCCESS);

        return new MentorDashboardResponse(
                completed,
                pending,
                confirmed,
                upcoming,
                reviewRepository.countByMentor_IdAndStatus(mentor.getId(), ReviewStatus.APPROVED),
                mentor.getAverageRating() == null ? BigDecimal.ZERO : mentor.getAverageRating(),
                zeroIfNull(grossPayments)
        );
    }

    @Override
    public PlatformAnalyticsResponse getPlatformAnalytics() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return new PlatformAnalyticsResponse(
                userRepository.count(),
                mentorProfileRepository.countByApprovalStatus(ApprovalStatus.APPROVED),
                mentorProfileRepository.countByApprovalStatus(ApprovalStatus.PENDING),
                sessionRepository.count(),
                sessionRepository.countByCreatedAtAfter(thirtyDaysAgo),
                sessionRepository.countByStatus(SessionStatus.PENDING),
                sessionRepository.countByStatus(SessionStatus.COMPLETED),
                sessionRepository.countByStatus(SessionStatus.CANCELLED),
                reviewRepository.countByStatus(ReviewStatus.PENDING),
                zeroIfNull(paymentRepository.sumAmountByStatus(PaymentStatus.SUCCESS))
        );
    }

    private BigDecimal zeroIfNull(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
