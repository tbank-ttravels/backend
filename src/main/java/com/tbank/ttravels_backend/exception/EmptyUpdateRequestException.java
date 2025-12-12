package com.tbank.ttravels_backend.exception;

public class EmptyUpdateRequestException extends RuntimeException{
    public EmptyUpdateRequestException(String message) {
        super(message);
    }
}
