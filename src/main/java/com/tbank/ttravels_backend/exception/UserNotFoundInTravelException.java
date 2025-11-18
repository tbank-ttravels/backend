package com.tbank.ttravels_backend.exception;

public class UserNotFoundInTravelException extends RuntimeException {

    public UserNotFoundInTravelException(Long userId, Long travelId) {
        super("Пользователь с id = " + userId + " не найден в поездке с id = " + travelId);
    }
}
