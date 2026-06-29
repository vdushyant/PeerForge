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
import com.peerforge.payment.gateway.dto.GatewayOrder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PaymentOrderServiceImpl implements PaymentOrderService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentLifecycleService paymentLifecycleService;
    private final PaymentGateway paymentGateway;

    @Value("${razorpay.key-id}")
    private String razorpayKey;

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

        GatewayOrder gatewayOrder =
                paymentGateway.createOrder(
                        amount,
                        "session_" + sessionId
                );

        Payment payment = Payment.builder()
                .session(session)
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .providerOrderId(
                        gatewayOrder.orderId()
                )
                .build();

        Payment saved = paymentRepository.save(payment);
        return new CreateOrderResponse(
                saved.getId(),
                session.getId(),
                saved.getProviderOrderId(),
                razorpayKey,
                saved.getAmount(),
                gatewayOrder.currency()
        );
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
