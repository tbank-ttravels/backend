package com.tbank.ttravels_backend.exception;

public class PayerNotInParticipantsException extends RuntimeException {
    public PayerNotInParticipantsException(String message) {
        super(message);
    }
}
