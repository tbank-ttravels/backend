package com.tbank.ttravels_backend.exception;

public class MemberExpenseNotFoundException extends RuntimeException {
    public MemberExpenseNotFoundException(Long userId) {
        super("Не найден участник траты с id = " + userId);
    }
}
