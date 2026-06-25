package com.peerforge.payment.controller;

import com.peerforge.payment.dto.response.PaymentResponse;
import com.peerforge.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor

public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/session/{sessionId}")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(
            @PathVariable
            Long sessionId,
            @AuthenticationPrincipal
            UserDetails userDetails
    ) {
        return paymentService.createPayment(
                        sessionId,
                        userDetails.getUsername()
                );
    }

    @PatchMapping("/{id}/success")
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentResponse markPaymentSuccess(
            @PathVariable
            Long id
    ) {
        return paymentService.markPaymentSuccess(id);
    }

    @PatchMapping("/{id}/failed")
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentResponse markPaymentFailed(
            @PathVariable
            Long id
    ) {
        return paymentService.markPaymentFailed(id);
    }

    @PatchMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentResponse refundPayment(
            @PathVariable
            Long id
    ) {
        return paymentService.refundPayment(id);
    }
}
