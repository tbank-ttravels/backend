package com.tbank.ttravels_backend.exception;

public class UserNotFoundExpenseException extends RuntimeException {
    public UserNotFoundExpenseException(Long userId, String nameExpense) {
        super("Пользовать с id = " + userId + " не найден в трате \"" + nameExpense + "\"");
    }
}
