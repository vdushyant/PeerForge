package com.peerforge.payment.service.impl;

import com.peerforge.common.exception.*;
import com.peerforge.mentor.entity.MentorProfile;
import com.peerforge.payment.dto.request.VerifyPaymentRequest;
import com.peerforge.payment.dto.response.CreateOrderResponse;
import com.peerforge.payment.dto.response.PaymentResponse;
import com.peerforge.payment.entity.Payment;
import com.peerforge.payment.entity.PaymentStatus;
import com.peerforge.payment.gateway.PaymentGateway;
import com.peerforge.payment.repository.PaymentRepository;
import com.peerforge.payment.service.PaymentLifecycleService;
import com.peerforge.payment.service.PaymentOrderService;
import com.peerforge.session.entity.Session;
import com.peerforge.session.entity.SessionStatus;
import com.peerforge.session.repository.SessionRepository;
import com.peerforge.user.entity.User;
import com.peerforge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.peerforge.payment.gateway.dto.GatewayQrCode;
import com.peerforge.payment.mapper.PaymentMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PaymentOrderServiceImpl implements PaymentOrderService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentLifecycleService paymentLifecycleService;
    private final PaymentGateway paymentGateway;
    private final PaymentMapper paymentMapper;

    @Value("${peerforge.payments.client-fee-percent:0}")
    private BigDecimal clientFeePercent;

    @Override
    @Transactional
    public CreateOrderResponse createOrder(
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

        if (session.getStatus() != SessionStatus.PENDING) {
            throw new InvalidSessionStateException(
                    "Payment can only be created for pending sessions"
            );
        }
        if (clientFeePercent.signum() < 0
                || clientFeePercent.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalStateException("PeerForge client fee percentage must be between 0 and 100");
        }

        var existingPayment =
                paymentRepository.findBySessionId(sessionId);

        if (existingPayment.isPresent()) {
            Payment payment = existingPayment.get();

            if (payment.getStatus() == PaymentStatus.PENDING
                    && payment.getProviderQrCodeId() != null
                    && payment.getProviderQrCodeExpiresAt() != null
                    && payment.getProviderQrCodeExpiresAt().isAfter(Instant.now())) {
                return toQrResponse(payment, "INR");
            }

            if (payment.getStatus() != PaymentStatus.FAILED
                    && payment.getStatus() != PaymentStatus.PENDING) {
                throw new InvalidPaymentStateException(
                        "Payment cannot be retried"
                );
            }
        }

        MentorProfile mentor = session.getMentor();

        BigDecimal hourlyRate = mentor.getHourlyRate();

        long durationMinutes = Duration.between(
                session.getStartDateTime(),
                session.getEndDateTime()
        ).toMinutes();

        BigDecimal durationHours =
                BigDecimal.valueOf(durationMinutes)
                        .divide(
                                BigDecimal.valueOf(60),
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal mentorAmount =
                hourlyRate.multiply(durationHours).setScale(2, RoundingMode.HALF_UP);

        BigDecimal clientFee = mentorAmount
                .multiply(clientFeePercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal amount = mentorAmount.add(clientFee);

        Payment payment;

        if (existingPayment.isPresent()) {
            payment = existingPayment.get();

            payment.setStatus(PaymentStatus.PENDING);
            payment.setAmount(amount);
            payment.setMentorAmount(mentorAmount);
            payment.setClientFee(clientFee);
            payment.setProviderOrderId(null);
            payment.setProviderPaymentId(null);
            payment.setProviderSignature(null);
            payment.setProviderQrCodeId(null);
            payment.setProviderQrCodeImageUrl(null);
            payment.setProviderQrCodeExpiresAt(null);
        } else {
            payment = Payment.builder()
                    .session(session)
                    .amount(amount)
                    .mentorAmount(mentorAmount)
                    .clientFee(clientFee)
                    .status(PaymentStatus.PENDING)
                    .build();
        }

        Payment saved = paymentRepository.saveAndFlush(payment);
        GatewayQrCode qrCode = paymentGateway.createQrCode(
                amount,
                "session_" + sessionId,
                saved.getId(),
                sessionId
        );
        saved.setProviderQrCodeId(qrCode.qrCodeId());
        saved.setProviderQrCodeImageUrl(qrCode.imageUrl());
        saved.setProviderQrCodeExpiresAt(qrCode.expiresAt());
        return toQrResponse(paymentRepository.save(saved), "INR");
    }

    @Override
    @Transactional
    public PaymentResponse verifyPayment(
            VerifyPaymentRequest request
    ) {

        Payment payment = paymentRepository
                .findByProviderOrderId(
                        request.razorpayOrderId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment not found"
                        ));


        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentStateException(
                    "Payment has already been processed"
            );

        }


        paymentGateway.verifyPayment(
                request.razorpayOrderId(),
                request.razorpayPaymentId(),
                request.razorpaySignature()
        );


        payment.setProviderPaymentId(
                request.razorpayPaymentId()
        );

        payment.setProviderSignature(
                request.razorpaySignature()
        );
        paymentRepository.save(payment);

        return paymentLifecycleService.completePayment(
                payment.getId()
        );

    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getSessionPaymentStatus(Long sessionId, String email) {
        User currentUser = getCurrentUser(email);
        Session session = getSession(sessionId);
        if (!session.getClient().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the booking client can view payment status");
        }
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        return paymentMapper.toResponse(payment);
    }

    private CreateOrderResponse toQrResponse(Payment payment, String currency) {
        return new CreateOrderResponse(
                payment.getId(), payment.getSession().getId(), null, null,
                payment.getAmount(), payment.getMentorAmount(), payment.getClientFee(), currency,
                payment.getProviderQrCodeId(), payment.getProviderQrCodeImageUrl(),
                payment.getProviderQrCodeExpiresAt()
        );
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
