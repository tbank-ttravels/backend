package com.tbank.ttravels_backend.exception;

public class PayerNotInParticipantsException extends RuntimeException {
    public PayerNotInParticipantsException(Long payerId) {
        super("Плательщик должен участвовать в трате id = " + payerId);
    }
}
