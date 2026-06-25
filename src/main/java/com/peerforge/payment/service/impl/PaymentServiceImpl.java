package com.peerforge.payment.service.impl;

import com.peerforge.common.exception.InvalidPaymentStateException;
import com.peerforge.common.exception.InvalidSessionStateException;
import com.peerforge.common.exception.PaymentAlreadyExistsException;
import com.peerforge.common.exception.ResourceNotFoundException;
import com.peerforge.mentor.entity.MentorProfile;
import com.peerforge.payment.dto.response.PaymentResponse;
import com.peerforge.payment.entity.Payment;
import com.peerforge.payment.entity.PaymentStatus;
import com.peerforge.payment.mapper.PaymentMapper;
import com.peerforge.payment.repository.PaymentRepository;
import com.peerforge.payment.service.PaymentService;
import com.peerforge.session.entity.Session;
import com.peerforge.session.entity.SessionStatus;
import com.peerforge.session.repository.SessionRepository;
import com.peerforge.user.entity.User;
import com.peerforge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentResponse createPayment(
            Long sessionId,
            String email
    ) {

        User currentUser = getCurrentUser(email);
        Session session = getSession(sessionId);


        if (!session.getClient().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    "Only client can create payment"
            );
        }


        paymentRepository.findBySessionId(sessionId)
                .ifPresent(payment -> {
                    throw new PaymentAlreadyExistsException("Payment already exists");
                });

        if (session.getStatus() != SessionStatus.PENDING) {
            throw new InvalidSessionStateException(
                    "Payment can only be created for pending sessions"
            );
        }

        MentorProfile mentor = session.getMentor();
        BigDecimal hourlyRate = mentor.getHourlyRate();

        long durationMinutes = Duration.between(
                        session.getStartDateTime(),
                        session.getEndDateTime()
                ).toMinutes();


        BigDecimal durationHours = BigDecimal.valueOf(durationMinutes).divide(
                                BigDecimal.valueOf(60),
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal amount = hourlyRate.multiply(durationHours);
        Payment payment = Payment.builder()
                        .session(session)
                        .amount(amount)
                        .status(PaymentStatus.PENDING)
                        .build();


        Payment saved = paymentRepository.save(payment);
        return paymentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PaymentResponse markPaymentSuccess(Long paymentId) {
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
