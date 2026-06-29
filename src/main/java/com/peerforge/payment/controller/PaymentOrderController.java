package com.peerforge.payment.controller;

import com.peerforge.payment.dto.request.VerifyPaymentRequest;
import com.peerforge.payment.dto.response.PaymentResponse;
import com.peerforge.payment.dto.response.CreateOrderResponse;
import com.peerforge.payment.service.PaymentOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments/orders")
@RequiredArgsConstructor

public class PaymentOrderController {
    private final PaymentOrderService paymentOrderService;

    @PostMapping("/session/{sessionId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrderResponse  createOrder(
            @PathVariable
            Long sessionId,
            @AuthenticationPrincipal
            UserDetails userDetails
    ) {
        return paymentOrderService.createOrder(
                sessionId,
                userDetails.getUsername()
        );
    }

    @PostMapping("/verify")
    public PaymentResponse verifyPayment(
            @Valid
            @RequestBody
            VerifyPaymentRequest request
    ) {
        return paymentOrderService.verifyPayment(
                request
        );
    }
}
