package com.tbank.ttravels_backend.exception;

public class DuplicateExpenseException extends RuntimeException {
    public DuplicateExpenseException(String message) {
        super(message);
    }
}
