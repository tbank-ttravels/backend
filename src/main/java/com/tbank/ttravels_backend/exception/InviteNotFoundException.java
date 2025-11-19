package com.tbank.ttravels_backend.exception;

public class InviteNotFoundException extends RuntimeException {
    public InviteNotFoundException(Long userId, Long inviteId) {
        super("Для пользователя с id = " + userId + " не найдено приглашение с id = " + inviteId);
    }
}
