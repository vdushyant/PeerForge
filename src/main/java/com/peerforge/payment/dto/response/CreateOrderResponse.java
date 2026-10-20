package com.peerforge.payment.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateOrderResponse(

        Long paymentId,

        Long sessionId,

        String razorpayOrderId,

        String razorpayKey,

        BigDecimal amount,

        BigDecimal mentorAmount,

        BigDecimal clientFee,

        String currency,

        String razorpayQrCodeId,

        String razorpayQrCodeImageUrl,

        Instant razorpayQrCodeExpiresAt

) {
}
