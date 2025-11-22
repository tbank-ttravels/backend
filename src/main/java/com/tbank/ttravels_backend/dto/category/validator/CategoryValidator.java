package com.tbank.ttravels_backend.dto.category.validator;

import com.tbank.ttravels_backend.dto.category.CreateCategoryRequest;
import com.tbank.ttravels_backend.exception.Validation;

public class CategoryValidator {

    public static void validateCreate(CreateCategoryRequest req) {
        if (req.getTravelId() == null)
            throw new Validation("travelId обязателен");

        if (req.getName() == null || req.getName().isBlank())
            throw new Validation("Название категории обязательно");
    }

    public static void validateEdit(String name) {
        if (name == null || name.isBlank())
            throw new Validation("Название категории обязательно");
    }
}
