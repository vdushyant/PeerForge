package com.peerforge.payment.controller;

import com.peerforge.payment.dto.response.PaymentResponse;
import com.peerforge.payment.service.PaymentLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor

public class PaymentController {

    private final PaymentLifecycleService paymentLifecycleService;

    @PatchMapping("/{id}/success")
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentResponse markPaymentSuccess(
            @PathVariable
            Long id
    ) {
        return paymentLifecycleService.completePayment(id);
    }

    @PatchMapping("/{id}/failed")
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentResponse markPaymentFailed(
            @PathVariable
            Long id
    ) {
        return paymentLifecycleService.markPaymentFailed(id);
    }

    @PatchMapping("/{id}/client-failed")
    public PaymentResponse markClientPaymentFailed(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return paymentLifecycleService.markClientPaymentFailed(id, userDetails.getUsername());
    }

    @PatchMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentResponse refundPayment(
            @PathVariable
            Long id
    ) {
        return paymentLifecycleService.refundPayment(id);
    }

}
