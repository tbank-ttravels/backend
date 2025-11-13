package com.tbank.ttravels_backend.exception;

public class TravelNotFound extends RuntimeException {
    public TravelNotFound(String message) {
        super(message);
    }
}
