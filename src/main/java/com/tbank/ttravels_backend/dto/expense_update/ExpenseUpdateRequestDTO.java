package com.tbank.ttravels_backend.dto.expense_update;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;


public record ExpenseUpdateRequestDTO(
        // Безопасные поля
        String name,
        String description,
        OffsetDateTime date,
        Long categoryId,
        // Опасные поля, необходимо передавать вместе (либо оба, либо ни один)
        BigDecimal sum,
        Map<Long, BigDecimal> participantShares) {

}
