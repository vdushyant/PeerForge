package com.peerforge.payment.controller;

import com.peerforge.payment.service.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    @PostMapping("/razorpay")
    @ResponseStatus(HttpStatus.OK)
    public void razorpayWebhook(
            @RequestBody
            String payload,
            @RequestHeader("X-Razorpay-Signature")
            String signature
    ) {
        webhookService.processRazorpayWebhook(
                payload,
                signature
        );

    }
}