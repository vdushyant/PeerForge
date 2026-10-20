package com.peerforge.payment.gateway;

import com.peerforge.common.exception.PaymentGatewayException;
import com.peerforge.payment.gateway.dto.GatewayOrder;
import com.peerforge.payment.gateway.dto.GatewayQrCode;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.QrCode;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
                ).setScale(0, java.math.RoundingMode.HALF_UP).intValueExact()
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
    public GatewayQrCode createQrCode(BigDecimal amount, String receipt, Long paymentId, Long sessionId) {
        JSONObject notes = new JSONObject();
        notes.put("peerforge_payment_id", paymentId.toString());
        notes.put("peerforge_session_id", sessionId.toString());
        JSONObject options = new JSONObject();
        options.put("type", "upi_qr");
        options.put("name", "PeerForge session booking");
        options.put("usage", "single_use");
        options.put("fixed_amount", true);
        options.put("payment_amount", amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, java.math.RoundingMode.HALF_UP).intValueExact());
        options.put("description", receipt);
        options.put("close_by", Instant.now().plus(115, ChronoUnit.MINUTES).getEpochSecond());
        options.put("notes", notes);
        try {
            QrCode qr = razorpayClient.qrCode.create(options);
            Instant expiresAt = Instant.ofEpochSecond(Long.parseLong(qr.get("close_by").toString()));
            return new GatewayQrCode(qr.get("id").toString(), qr.get("image_url").toString(), expiresAt);
        } catch (RazorpayException ex) {
            throw new PaymentGatewayException("Failed to create Razorpay QR code", ex);
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
