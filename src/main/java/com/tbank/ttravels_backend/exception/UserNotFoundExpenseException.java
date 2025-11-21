package com.tbank.ttravels_backend.exception;

public class UserNotFoundExpenseException extends RuntimeException {
    public UserNotFoundExpenseException(String message) {
        super(message);
    }
}
