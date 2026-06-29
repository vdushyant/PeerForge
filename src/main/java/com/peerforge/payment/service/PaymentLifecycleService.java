package com.peerforge.payment.service;

import com.peerforge.payment.dto.request.VerifyPaymentRequest;
import com.peerforge.payment.dto.response.CreateOrderResponse;
import com.peerforge.payment.dto.response.PaymentResponse;

public interface PaymentLifecycleService {

    PaymentResponse completePayment(
            Long paymentId
    );

    PaymentResponse markPaymentFailed(
            Long paymentId
    );

    PaymentResponse refundPayment(
            Long paymentId
    );

}