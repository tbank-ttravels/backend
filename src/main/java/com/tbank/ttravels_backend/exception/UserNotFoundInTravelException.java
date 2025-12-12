package com.tbank.ttravels_backend.exception;

public class UserNotFoundInTravelException extends RuntimeException {

    public UserNotFoundInTravelException(String message) {
        super(message);
    }
}
