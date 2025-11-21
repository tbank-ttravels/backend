package com.tbank.ttravels_backend.exception;

public class UserNotFoundByPhoneException extends RuntimeException {
    public UserNotFoundByPhoneException(String message) {
        super(message);
    }
}
