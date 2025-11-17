package com.tbank.ttravels_backend.exception;

public class InvalidParticipantShareException extends RuntimeException {
    public InvalidParticipantShareException(Long userId, String s) {
        super(s + " (id = " + userId + ")");
    }

    public InvalidParticipantShareException() {
        super("Расход должен содержать хотя бы одного участника");
    }
}
