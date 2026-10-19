package com.peerforge.payment.dto.response;

import com.peerforge.payment.entity.PaymentStatus;

import java.math.BigDecimal;

public record PaymentResponse(

        Long id,

        Long sessionId,

        BigDecimal amount,

        BigDecimal mentorAmount,

        BigDecimal clientFee,

        PaymentStatus status,

        String providerOrderId,

        String providerPaymentId,

        String razorpayKey

) {
}
