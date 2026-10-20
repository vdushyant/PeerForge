package com.peerforge.payment.gateway;

import com.peerforge.payment.gateway.dto.GatewayOrder;
import com.peerforge.payment.gateway.dto.GatewayQrCode;

import java.math.BigDecimal;

public interface PaymentGateway {

    GatewayOrder createOrder(
            BigDecimal amount,
            String receipt
    );

    GatewayQrCode createQrCode(BigDecimal amount, String receipt, Long paymentId, Long sessionId);

    void verifyPayment(
            String orderId,
            String paymentId,
            String signature
    );

}
