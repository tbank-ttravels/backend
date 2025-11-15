package com.tbank.ttravels_backend.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userId) {
        super("Пользователь с id = " + userId + " не найден");
    }
}