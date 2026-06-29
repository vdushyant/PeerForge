package com.peerforge.payment.service.impl;

import com.peerforge.payment.service.WebhookService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;

public class WebhookServiceImpl implements WebhookService {

    @Override
    public void processRazorpayWebhook(String payload, String signature) {

    }
}
