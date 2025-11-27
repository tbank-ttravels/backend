package com.tbank.ttravels_backend.exception;

public class ExpenseNotFoundInTravelException extends RuntimeException {
    public ExpenseNotFoundInTravelException(Long expenseId, Long travelId) {
        super("Трата с id = "+ expenseId + " не найдена в поездке с id = " + travelId);
    }
}
