package com.tbank.ttravels_backend.exception;

public class ConflictStateException extends RuntimeException {
    public ConflictStateException(String message) {
        super(message);
    }
}
