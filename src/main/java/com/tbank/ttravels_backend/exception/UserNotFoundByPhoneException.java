package com.tbank.ttravels_backend.exception;

public class UserNotFoundByPhoneException extends RuntimeException {
    public UserNotFoundByPhoneException(String phone) {
        super("Пользователь с phone = " + phone + " не найден");
    }
}
