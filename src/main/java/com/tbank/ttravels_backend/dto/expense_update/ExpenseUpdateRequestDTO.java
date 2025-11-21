package com.tbank.ttravels_backend.dto.expense_update;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;


public record ExpenseUpdateRequestDTO(
        String name,
        String description,
        OffsetDateTime date,
        Long categoryId,
        Long payerId,
        Map<Long, BigDecimal> participantShares) {

}
