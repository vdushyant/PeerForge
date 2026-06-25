package com.peerforge.common.exception;

public class InvalidPaymentStateException
        extends RuntimeException {

    public InvalidPaymentStateException(
            String message
    ) {
        super(message);
    }

}