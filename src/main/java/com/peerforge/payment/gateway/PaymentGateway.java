package com.peerforge.payment.gateway;

import com.peerforge.payment.gateway.dto.GatewayOrder;

import java.math.BigDecimal;

public interface PaymentGateway {

    GatewayOrder createOrder(
            BigDecimal amount,
            String receipt
    );

    void verifyPayment(
            String orderId,
            String paymentId,
            String signature
    );

}