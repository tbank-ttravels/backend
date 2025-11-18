package com.tbank.ttravels_backend.dto.transfer.validator;

import com.tbank.ttravels_backend.dto.transfer.CreateTransferRequest;
import com.tbank.ttravels_backend.dto.transfer.EditTransferRequest;
import com.tbank.ttravels_backend.exception.Validation;

import java.math.BigDecimal;

public class CreateTransferValidator {

    public static void validateCreate(CreateTransferRequest req) {
        if (req.getTravelId() == null)
            throw new Validation("Не указан travelId");

        if (req.getSenderId() == null)
            throw new Validation("Не указан senderId");

        if (req.getRecipientId() == null)
            throw new Validation("Не указан recipientId");

        if (req.getSum() == null || req.getSum().compareTo(BigDecimal.ZERO) <= 0)
            throw new Validation("Сумма должна быть больше 0");

        if (req.getSenderId().equals(req.getRecipientId()))
            throw new Validation("Отправитель и получатель не могут совпадать");
    }

    public static void validateEdit(EditTransferRequest req) {
        if (req.getSum() != null && req.getSum().compareTo(BigDecimal.ZERO) <= 0)
            throw new Validation("Сумма должна быть больше 0");
    }
}
