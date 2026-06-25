package com.peerforge.common.exception;

public class InvalidSessionStateException
        extends RuntimeException {

    public InvalidSessionStateException(
            String message
    ) {
        super(message);
    }

}