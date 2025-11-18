package com.tbank.ttravels_backend.exception;

public class EmptyUpdateRequestException extends RuntimeException{
    public EmptyUpdateRequestException() {
        super("Отсутствуют поля для обновления траты");
    }
}
