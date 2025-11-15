package com.tbank.ttravels_backend.exception;

public class PayerShareInvalidException extends RuntimeException {
    public PayerShareInvalidException(Long userId) {
        super("Сумма доли плательщика с id = " + userId + "некорректна");
    }
}
