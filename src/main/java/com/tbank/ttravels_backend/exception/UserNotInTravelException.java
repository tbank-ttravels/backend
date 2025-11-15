package com.tbank.ttravels_backend.exception;

public class UserNotInTravelException extends RuntimeException {

    public UserNotInTravelException(Long userId, Long travelId) {
        super("Пользователь с id = " + userId + " не найден в поездке с id = " + travelId);
    }
}
