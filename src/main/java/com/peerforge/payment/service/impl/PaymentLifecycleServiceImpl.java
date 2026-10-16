package com.peerforge.payment.service.impl;

import com.peerforge.common.exception.*;
import com.peerforge.payment.dto.response.PaymentResponse;
import com.peerforge.payment.entity.Payment;
import com.peerforge.payment.entity.PaymentStatus;
import com.peerforge.payment.mapper.PaymentMapper;
import com.peerforge.payment.repository.PaymentRepository;
import com.peerforge.payment.service.PaymentLifecycleService;
import com.peerforge.notification.entity.NotificationType;
import com.peerforge.notification.service.NotificationService;
import com.peerforge.session.entity.Session;
import com.peerforge.session.entity.SessionStatus;
import com.peerforge.session.repository.SessionRepository;
import com.peerforge.user.entity.User;
import com.peerforge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentLifecycleServiceImpl implements PaymentLifecycleService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final NotificationService notificationService;

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
        notificationService.notifyUser(
                session.getClient(), NotificationType.PAYMENT_SUCCESS,
                "Payment successful",
                "Your payment was received and the session is confirmed.",
                "/sessions"
        );
        notificationService.notifyUser(
                session.getMentor().getUser(), NotificationType.SESSION_CONFIRMED,
                "Session confirmed",
                "A paid session with " + session.getClient().getFirstName() + " is confirmed.",
                "/mentor/sessions"
        );
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
        notifyClientPaymentFailed(payment);
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
        Session session = payment.getSession();
        notificationService.notifyUser(
                session.getClient(), NotificationType.PAYMENT_REFUNDED,
                "Payment refunded",
                "Your payment was refunded and the session was cancelled.",
                "/sessions"
        );
        notificationService.notifyUser(
                session.getMentor().getUser(), NotificationType.SESSION_CANCELLED,
                "Session cancelled",
                "The session with " + session.getClient().getFirstName()
                        + " was cancelled after a refund.",
                "/mentor/sessions"
        );
        return paymentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PaymentResponse markClientPaymentFailed(
            Long paymentId,
            String email
    ) {
        User currentUser = getCurrentUser(email);

        Payment payment = getPayment(paymentId);

        if (!payment.getSession()
                .getClient()
                .getId()
                .equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    "You are not allowed to modify this payment"
            );
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentStateException(
                    "Only pending payments can be marked failed"
            );
        }

        payment.setStatus(PaymentStatus.FAILED);

        Payment saved =
                paymentRepository.save(payment);
        notifyClientPaymentFailed(payment);

        return paymentMapper.toResponse(saved);
    }

    private void notifyClientPaymentFailed(Payment payment) {
        notificationService.notifyUser(
                payment.getSession().getClient(), NotificationType.PAYMENT_FAILED,
                "Payment not completed",
                "We could not confirm payment for your session. You can try again from your sessions.",
                "/sessions"
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
