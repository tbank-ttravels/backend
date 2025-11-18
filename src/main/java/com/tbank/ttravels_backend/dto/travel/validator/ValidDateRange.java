package com.tbank.ttravels_backend.dto.travel.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DateRangeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {
    String message() default "Дата окончания должна быть позже даты начала!";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}