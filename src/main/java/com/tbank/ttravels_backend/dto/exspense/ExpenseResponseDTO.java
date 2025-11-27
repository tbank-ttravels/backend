package com.tbank.ttravels_backend.dto.exspense;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;

/**
 * @param id           ID траты
 * @param name         Название
 * @param payerId      ID платящего
 * @param description  Описание
 * @param sum          Сумма
 * @param date         Дата расхода
 * @param categoryId   ID категории
 * @param categoryName Название категории
 * @param participants Список участников
 */

@Builder
@Schema(description = "DTO для отображения информации о трате")
public record ExpenseResponseDTO(
        @Schema(description = "ID траты", example = "123") Long id,
        @Schema(description = "ID плательщика", example = "45") Long payerId,
        @Schema(description = "Название траты", example = "Ужин в ресторане") String name,
        @Schema(description = "Описание траты", example = "Ужин с друзьями после похода") String description,
        @Schema(description = "Сумма траты", example = "1200.50") BigDecimal sum,
        @Schema(description = "Дата траты", example = "2025-11-22T18:30:00Z") OffsetDateTime date,
        @Schema(description = "ID категории", example = "7") Long categoryId,
        @Schema(description = "Название категории", example = "Питание") String categoryName,
        @ArraySchema(
                arraySchema = @Schema(description = "Список участников траты"),
                schema = @Schema(implementation = MemberExpenseResponseDTO.class)
        ) Set<MemberExpenseResponseDTO> participants
) {}
