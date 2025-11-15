package com.tbank.ttravels_backend.exception;

public class ExpenseNotInTravelException extends RuntimeException {
    public ExpenseNotInTravelException(Long expenseId, Long travelId) {
        super("Трата с id = "+ expenseId + " не найдена в поездке с id = " + travelId);
    }
}
