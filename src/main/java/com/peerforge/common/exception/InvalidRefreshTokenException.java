package com.peerforge.common.exception;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException(
            String message
    ) {
        super(message);
    }
}