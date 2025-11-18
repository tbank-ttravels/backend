package com.tbank.ttravels_backend.exception;

public class OwnerRemovalNotAllowedException extends RuntimeException {
    public OwnerRemovalNotAllowedException(String message) {
        super(message);
    }
}
