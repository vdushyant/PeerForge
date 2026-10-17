package com.peerforge.common.exception;

public class MailDeliveryUnavailableException extends RuntimeException {
    public MailDeliveryUnavailableException() {
        super("Email delivery is not configured or is temporarily unavailable");
    }
}
