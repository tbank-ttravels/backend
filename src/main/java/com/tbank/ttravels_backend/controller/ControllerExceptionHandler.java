package com.tbank.ttravels_backend.controller;

import com.tbank.ttravels_backend.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.OffsetDateTime;
import java.util.Map;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler({
            CategoryNotFoundException.class,
            ExpenseNotFoundInTravelException.class,
            TravelNotFoundException.class,
            UserNotFoundInTravelException.class,
            UserNotFoundException.class,
            EmptyUpdateRequestException.class,
            InitiatorNoAccessException.class,
            PayerNotInParticipantsException.class,
            CannotRemovePayerFromExpenseException.class,
            EmptyParticipantsListException.class,
            MemberExpenseNotFoundException.class,
            UsersNotFoundInExpenseException.class
    })
    public ResponseEntity<Object> handleNotFound(RuntimeException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorBody(ex));
    }


    @ExceptionHandler({
            InvalidParticipantShareException.class,
            PayerShareInvalidException.class
    })
    public ResponseEntity<Object> handle(RuntimeException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorBody(ex));
    }


    private Map<String, Object> createErrorBody(RuntimeException ex) {

        return Map.of(
                "error", ex.getClass().getSimpleName(),
                "message", ex.getMessage(),
                "timestamp", OffsetDateTime.now()
        );
    }
}
