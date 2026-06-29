package com.peerforge.payment.service.impl;

import com.peerforge.common.exception.*;
import com.peerforge.payment.dto.response.PaymentResponse;
import com.peerforge.payment.entity.Payment;
import com.peerforge.payment.entity.PaymentStatus;
import com.peerforge.payment.mapper.PaymentMapper;
import com.peerforge.payment.repository.PaymentRepository;
import com.peerforge.payment.service.PaymentLifecycleService;
import com.peerforge.session.entity.Session;
import com.peerforge.session.entity.SessionStatus;
import com.peerforge.session.repository.SessionRepository;
import com.peerforge.user.entity.User;
import com.peerforge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentLifecycleServiceImpl implements PaymentLifecycleService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentResponse completePayment(Long paymentId) {
        Payment payment = getPayment(paymentId);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentStateException(
                    "Only pending payments can be marked successful"
            );
        }

        if (payment.getSession().getStatus() == SessionStatus.CANCELLED) {
            throw new InvalidSessionStateException(
                    "Cannot process payment for cancelled session"
            );
        }

        Session session = payment.getSession();
        session.setStatus(SessionStatus.CONFIRMED);

        payment.setStatus(PaymentStatus.SUCCESS);
        Payment saved = paymentRepository.save(payment);
        return paymentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PaymentResponse markPaymentFailed(
            Long paymentId
    ) {
        Payment payment = getPayment(paymentId);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentStateException("Only pending payments can be marked failed");
        }

        payment.setStatus(PaymentStatus.FAILED);
        Payment saved = paymentRepository.save(payment);
        return paymentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(
            Long paymentId
    ) {
        Payment payment = getPayment(paymentId);

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new InvalidPaymentStateException(
                    "Only successful payments can be refunded"
            );
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.getSession().setStatus(SessionStatus.CANCELLED);

        Payment saved = paymentRepository.save(payment);
        return paymentMapper.toResponse(saved);
    }

    // Lookup Helpers
    private User getCurrentUser(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));
    }

    private Session getSession(Long sessionId) {
        return sessionRepository
                .findById(sessionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Session not found"
                        ));
    }

    private Payment getPayment(Long paymentId) {
        return paymentRepository
                .findById(paymentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment not found"
                        ));
    }
}
