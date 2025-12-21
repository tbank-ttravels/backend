package com.tbank.ttravels_backend.exception;

public class CannotRemovePayerFromExpenseException extends RuntimeException {
    public CannotRemovePayerFromExpenseException(String message) {
        super(message);
    }
}
