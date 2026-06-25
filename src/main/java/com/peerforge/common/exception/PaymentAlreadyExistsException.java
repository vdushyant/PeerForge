package com.peerforge.common.exception;
public class PaymentAlreadyExistsException
        extends RuntimeException {

    public PaymentAlreadyExistsException(
            String message
    ) {
        super(message);
    }

}