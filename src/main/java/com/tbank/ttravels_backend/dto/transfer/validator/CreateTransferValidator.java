package com.tbank.ttravels_backend.dto.transfer.validator;

import com.tbank.ttravels_backend.dto.transfer.CreateTransferRequest;
import com.tbank.ttravels_backend.exception.Validation;

public class CreateTransferValidator {

    public static void validateCreate(CreateTransferRequest request) {
        if (request.getSenderId() == null) {
            throw new Validation("senderId обязателен");
        }
        if (request.getRecipientId() == null) {
            throw new Validation("recipientId обязателен");
        }
        if (request.getSum() == null || request.getSum().signum() <= 0) {
            throw new Validation("Сумма должна быть положительной");
        }
        if (request.getSenderId().equals(request.getRecipientId())) {
            throw new Validation("Отправитель и получатель не могут совпадать");
        }
    }
}
