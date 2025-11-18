package com.tbank.ttravels_backend.exception;

import java.util.Set;
import java.util.stream.Collectors;

public class DuplicateParticipantException extends RuntimeException {

    public DuplicateParticipantException(Set<Long> missingUsers, String name) {
        super("В трате \"" + name + "\"" + " уже участвуют пользователи с id: " +
                missingUsers.stream()
                        .sorted()
                        .map(Object::toString)
                        .collect(Collectors.joining(", ")));
    }
}
