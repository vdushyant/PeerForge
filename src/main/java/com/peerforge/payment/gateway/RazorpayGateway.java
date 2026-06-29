package com.peerforge.payment.gateway;

import com.peerforge.common.exception.PaymentGatewayException;
import com.peerforge.payment.gateway.dto.GatewayOrder;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RazorpayGateway implements PaymentGateway {

    private final RazorpayClient razorpayClient;
    @Value("${razorpay.key-secret}")
    private String keySecret;

    @Override
    public GatewayOrder createOrder(
            BigDecimal amount,
            String receipt
    ) {

        JSONObject options = new JSONObject();

        options.put(
                "amount",
                amount.multiply(
                        BigDecimal.valueOf(100)
                ).intValue()
        );

        options.put(
                "currency",
                "INR"
        );

        options.put(
                "receipt",
                receipt
        );

        try {

            Order order =
                    razorpayClient
                            .orders
                            .create(options);

            return new GatewayOrder(

                    order.get("id").toString(),

                    order.get("currency").toString()

            );

        } catch (RazorpayException ex) {

            throw new PaymentGatewayException(

                    "Failed to create Razorpay order",

                    ex

            );

        }

    }

    @Override
    public void verifyPayment(

            String orderId,

            String paymentId,

            String signature

    ) {

        JSONObject attributes = new JSONObject();

        attributes.put(
                "razorpay_order_id",
                orderId
        );

        attributes.put(
                "razorpay_payment_id",
                paymentId
        );

        attributes.put(
                "razorpay_signature",
                signature
        );

        try {

            Utils.verifyPaymentSignature(
                    attributes,
                    keySecret
            );

        } catch (RazorpayException ex) {

            throw new PaymentGatewayException(
                    "Payment verification failed",
                    ex
            );

        }

    }

}