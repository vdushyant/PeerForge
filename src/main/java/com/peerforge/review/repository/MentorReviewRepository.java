package com.peerforge.review.repository;

import com.peerforge.mentor.entity.MentorProfile;
import com.peerforge.review.entity.MentorReview;
import com.peerforge.review.entity.ReviewStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MentorReviewRepository extends JpaRepository<MentorReview, Long> {

    Optional<MentorReview> findBySession_Id(Long sessionId);

    List<MentorReview> findByMentor_IdAndStatusOrderByCreatedAtDesc(
            Long mentorId,
            ReviewStatus status
    );

    List<MentorReview> findAllByStatusOrderByCreatedAtAsc(ReviewStatus status);

    long countByMentor_IdAndStatus(Long mentorId, ReviewStatus status);

    @Query("select avg(r.rating) from MentorReview r where r.mentor.id = :mentorId and r.status = :status")
    Double findAverageRating(
            @Param("mentorId") Long mentorId,
            @Param("status") ReviewStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from MentorReview r where r.id = :reviewId")
    Optional<MentorReview> findByIdForUpdate(@Param("reviewId") Long reviewId);
}
