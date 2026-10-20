package com.peerforge.payment.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peerforge.payment.entity.Payment;
import com.peerforge.payment.entity.PaymentStatus;
import com.peerforge.session.entity.Session;
import com.peerforge.session.entity.SessionStatus;
import com.peerforge.payment.repository.PaymentRepository;
import com.peerforge.payment.service.WebhookService;
import com.peerforge.payment.service.PaymentLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService {

    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;
    private final PaymentLifecycleService paymentLifecycleService;

    @Value("${razorpay.webhook-secret}")
    private String webhookSecret;

    @Override
    @Transactional
    public void processRazorpayWebhook(String payload, String signature) {

        verifySignature(payload, signature);
        try {
            JsonNode root = objectMapper.readTree(payload);
            String event = root.path("event").asText();

            if ("qr_code.credited".equals(event)) {
                handleQrCodeCredited(root.path("payload"));
                return;
            }

            JsonNode paymentEntity = root.path("payload").path("payment").path("entity");
            String razorpayOrderId = paymentEntity.path("order_id").asText();

            String razorpayPaymentId = paymentEntity.path("id").asText();

            if (razorpayOrderId == null || razorpayOrderId.isBlank()) {
                return;
            }

            Payment payment = paymentRepository
                    .findByProviderOrderId(razorpayOrderId)
                    .orElse(null);

            if (payment == null) {
                return;
            }

            switch (event) {

                case "payment.captured" -> handlePaymentCaptured(
                        payment,
                        razorpayPaymentId
                );

                case "payment.failed" -> handlePaymentFailed(
                        payment,
                        razorpayPaymentId
                );

                default -> {
                    // Ignore events that PeerForge does not handle.
                }
            }

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to process Razorpay webhook",
                    exception
            );
        }
    }

    private void handleQrCodeCredited(JsonNode payload) {
        JsonNode qrEntity = payload.path("qr_code").path("entity");
        JsonNode paymentEntity = payload.path("payment").path("entity");
        String qrCodeId = qrEntity.path("id").asText("");
        String razorpayPaymentId = paymentEntity.path("id").asText("");
        if (qrCodeId.isBlank() || razorpayPaymentId.isBlank()) return;

        Payment payment = paymentRepository.findByProviderQrCodeId(qrCodeId).orElse(null);
        if (payment == null || payment.getStatus() != PaymentStatus.PENDING) return;
        if (!"captured".equalsIgnoreCase(paymentEntity.path("status").asText())
                || !paymentEntity.path("captured").asBoolean(false)
                || !"INR".equalsIgnoreCase(paymentEntity.path("currency").asText())
                || paymentEntity.path("amount").asLong(-1) != payment.getAmount()
                        .movePointRight(2).longValueExact()) {
            return;
        }
        if (payment.getSession().getStatus() == SessionStatus.CANCELLED) return;

        payment.setProviderPaymentId(razorpayPaymentId);
        paymentRepository.save(payment);
        paymentLifecycleService.completePayment(payment.getId());
    }

    private void handlePaymentCaptured(
            Payment payment,
            String razorpayPaymentId
    ) {

        // Idempotency:
        // Razorpay may deliver the same webhook more than once.
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        Session session = payment.getSession();

        if (session.getStatus() == SessionStatus.CANCELLED) {
            return;
        }

        payment.setProviderPaymentId(razorpayPaymentId);
        payment.setStatus(PaymentStatus.SUCCESS);

        session.setStatus(SessionStatus.CONFIRMED);

        paymentRepository.save(payment);
    }

    private void handlePaymentFailed(
            Payment payment,
            String razorpayPaymentId
    ) {

        // Idempotency.
        // Only pending payments may transition to failed. A later event
        // must not overwrite a terminal payment state or a retried order.
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        payment.setProviderPaymentId(razorpayPaymentId);
        payment.setStatus(PaymentStatus.FAILED);

        paymentRepository.save(payment);
    }

    private void verifySignature(
            String payload,
            String signature
    ) {

        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKey = new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );

            mac.init(secretKey);

            byte[] hash = mac.doFinal(
                    payload.getBytes(StandardCharsets.UTF_8)
            );

            if (signature == null || signature.isBlank()
                    || !MessageDigest.isEqual(
                    hash,
                    HexFormat.of().parseHex(signature))) {
                throw new SecurityException(
                        "Invalid Razorpay webhook signature"
                );
            }

        } catch (SecurityException exception) {
            throw exception;

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to verify Razorpay webhook signature",
                    exception
            );
        }
    }
}
