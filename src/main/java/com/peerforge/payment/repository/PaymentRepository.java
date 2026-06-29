package com.peerforge.payment.repository;

import com.peerforge.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findBySessionId(Long sessionId);
    Optional<Payment> findByProviderOrderId(
            String providerOrderId
    );
}

