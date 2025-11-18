package com.tbank.ttravels_backend.exception;


public class EmptyParticipantsListException extends RuntimeException {
    public EmptyParticipantsListException() {
        super("Список участников для удаления не может быть пустым");
    }
}