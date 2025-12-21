package com.tbank.ttravels_backend.dto.expense_update;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

@Schema(description = "DTO для обновления траты")
@Builder
public record ExpenseUpdateRequestDTO(
        @Schema(description = "Название траты", example = "Обед с друзьями")
        String name,

        @Schema(description = "Описание траты", example = "Поход в кафе")
        String description,

        @Schema(description = "Дата траты", example = "2025-11-22T15:30:00+03:00")
        OffsetDateTime date,

        @Schema(description = "ID категории траты", example = "2")
        Long categoryId,

        @Schema(description = "ID плательщика", example = "5")
        Long payerId,

        @Schema(description = "Карта участников и их долей (userId: сумма)")
        Map<Long, BigDecimal> participantShares) {

}
