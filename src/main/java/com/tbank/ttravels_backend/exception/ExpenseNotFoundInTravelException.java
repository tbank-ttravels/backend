package com.tbank.ttravels_backend.exception;

public class ExpenseNotFoundInTravelException extends RuntimeException {
    public ExpenseNotFoundInTravelException(String message) {
        super(message);
    }
}
