package com.peerforge.payment.service;

public interface WebhookService {

    void processRazorpayWebhook(
            String payload,
            String signature
    );

}