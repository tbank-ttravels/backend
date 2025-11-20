package com.tbank.ttravels_backend.exception;

import java.util.Set;
import java.util.stream.Collectors;

public class DuplicateParticipantException extends RuntimeException {

    public DuplicateParticipantException(String message) {
        super(message);
    }
}
