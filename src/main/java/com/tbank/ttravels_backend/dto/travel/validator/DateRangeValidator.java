package com.tbank.ttravels_backend.dto.travel.validator;

import com.tbank.ttravels_backend.dto.travel.CreateTravelRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, CreateTravelRequest> {

    @Override
    public boolean isValid(CreateTravelRequest request, ConstraintValidatorContext context) {
        if (request == null || request.getStartDate() == null || request.getEndDate() == null) {
            return true;
        }
        return request.getEndDate().isAfter(request.getStartDate());
    }
}
