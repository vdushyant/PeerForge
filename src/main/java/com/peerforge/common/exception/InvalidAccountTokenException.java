package com.peerforge.common.exception;

public class InvalidAccountTokenException extends RuntimeException {
    public InvalidAccountTokenException(String message) {
        super(message);
    }
}
