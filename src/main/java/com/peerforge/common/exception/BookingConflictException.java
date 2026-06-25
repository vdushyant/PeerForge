package com.peerforge.common.exception;

public class BookingConflictException
        extends RuntimeException {

    public BookingConflictException(
            String message
    ) {
        super(message);
    }

}