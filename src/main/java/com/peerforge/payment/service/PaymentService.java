package com.peerforge.payment.service;

import com.peerforge.payment.dto.response.PaymentResponse;

public interface PaymentService {

    PaymentResponse createPayment(
            Long sessionId,
            String email
    );

    PaymentResponse markPaymentSuccess(
            Long paymentId
    );

    PaymentResponse markPaymentFailed(
            Long paymentId
    );

    PaymentResponse refundPayment(
            Long paymentId
    );

}