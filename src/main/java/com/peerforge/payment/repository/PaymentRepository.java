package com.peerforge.payment.repository;

import com.peerforge.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.peerforge.payment.entity.PaymentStatus;

import java.math.BigDecimal;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findBySessionId(Long sessionId);
    Optional<Payment> findByProviderOrderId(
            String providerOrderId
    );

    Optional<Payment> findByProviderQrCodeId(String providerQrCodeId);

    @Query("select sum(p.amount) from Payment p where p.session.mentor.id = :mentorId and p.status = :status")
    BigDecimal sumAmountByMentorAndStatus(
            @Param("mentorId") Long mentorId,
            @Param("status") PaymentStatus status
    );

    @Query("select sum(p.amount) from Payment p where p.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);
}

