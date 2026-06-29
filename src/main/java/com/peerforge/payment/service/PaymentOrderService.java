package com.peerforge.payment.service;

import com.peerforge.payment.dto.request.VerifyPaymentRequest;
import com.peerforge.payment.dto.response.CreateOrderResponse;
import com.peerforge.payment.dto.response.PaymentResponse;

public interface PaymentOrderService {

    CreateOrderResponse createOrder(
            Long sessionId,
            String email
    );

    PaymentResponse verifyPayment(
            VerifyPaymentRequest request
    );


}