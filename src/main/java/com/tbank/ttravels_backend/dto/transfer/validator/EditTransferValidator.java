package com.tbank.ttravels_backend.dto.transfer.validator;

import com.tbank.ttravels_backend.dto.transfer.EditTransferRequest;
import com.tbank.ttravels_backend.exception.Validation;

public class EditTransferValidator {

    public static void validate(EditTransferRequest request) {
        if (request.getSum() == null) {
            throw new Validation("sum обязателен");
        }
        if (request.getSum().signum() <= 0) {
            throw new Validation("Сумма должна быть положительной");
        }
    }
}
