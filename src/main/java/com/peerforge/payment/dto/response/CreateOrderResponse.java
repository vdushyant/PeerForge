package com.peerforge.payment.dto.response;

import java.math.BigDecimal;

public record CreateOrderResponse(

        Long paymentId,

        Long sessionId,

        String razorpayOrderId,

        String razorpayKey,

        BigDecimal amount,

        String currency

) {
}