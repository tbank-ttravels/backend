package com.tbank.ttravels_backend.dto.transfer.validator;

import com.tbank.ttravels_backend.dto.transfer.EditTransferRequest;
import com.tbank.ttravels_backend.exception.Validation;

import java.math.BigDecimal;

public class EditTransferValidator {

    public static void validate(EditTransferRequest request) {

        if (request.getSum() == null)
            throw new Validation("Сумма обязательна");

        if (request.getSum().compareTo(BigDecimal.ZERO) <= 0)
            throw new Validation("Сумма должна быть больше 0");
    }
}

