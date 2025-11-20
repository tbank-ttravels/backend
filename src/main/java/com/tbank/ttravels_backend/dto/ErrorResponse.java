package com.tbank.ttravels_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(name = "ErrorResponse")
public record ErrorResponse(
        int status,
        String error,
        String message,
        OffsetDateTime timestamp
) {
}
