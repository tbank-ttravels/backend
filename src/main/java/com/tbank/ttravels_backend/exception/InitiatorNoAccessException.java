package com.tbank.ttravels_backend.exception;

public class InitiatorNoAccessException extends RuntimeException{
    public InitiatorNoAccessException(Long travelId) {
        super("У Вас нет доступа к этой поезкде с id + " + travelId);
    }
}
