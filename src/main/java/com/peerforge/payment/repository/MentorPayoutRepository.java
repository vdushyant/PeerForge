package com.peerforge.payment.repository;

import com.peerforge.payment.entity.MentorPayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface MentorPayoutRepository extends JpaRepository<MentorPayout, Long> {
    Optional<MentorPayout> findByPayment_Id(Long paymentId);

    @Query("select sum(p.amount) from MentorPayout p where p.mentor.id = :mentorId and p.status <> com.peerforge.payment.entity.MentorPayoutStatus.PAID and p.status <> com.peerforge.payment.entity.MentorPayoutStatus.REVERSED")
    BigDecimal sumOutstandingAmountByMentorId(@Param("mentorId") Long mentorId);
}
