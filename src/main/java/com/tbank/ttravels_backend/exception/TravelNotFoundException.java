package com.tbank.ttravels_backend.exception;

public class TravelNotFoundException extends RuntimeException {

    public TravelNotFoundException(Long travelId) {
        super("Поездка с id = " + travelId + " не найдена");
    }
}
