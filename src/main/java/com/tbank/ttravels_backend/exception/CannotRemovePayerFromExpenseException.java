package com.tbank.ttravels_backend.exception;

public class CannotRemovePayerFromExpenseException extends RuntimeException {
    public CannotRemovePayerFromExpenseException(Long id, String expenseName) {
        super("Невозможно удалить плательщикас с id = " + id + " из траты \"" + expenseName + "\"");
    }
}
