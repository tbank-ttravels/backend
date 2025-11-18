package com.tbank.ttravels_backend.exception;

import java.util.Set;
import java.util.stream.Collectors;

public class UsersNotFoundInExpenseException extends RuntimeException {
    public UsersNotFoundInExpenseException(Set<Long> missingUsers, String name) {
        super("В трате \"" + name + "\"" + " не найдены участники с id: " +
                missingUsers.stream()
                        .sorted()
                        .map(Object::toString)
                        .collect(Collectors.joining(", ")));
    }
}
