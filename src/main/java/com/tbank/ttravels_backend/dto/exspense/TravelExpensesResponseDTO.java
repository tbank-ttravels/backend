package com.tbank.ttravels_backend.dto.exspense;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Schema(description = "DTO со списком всех расходов и их общей суммой")
public record TravelExpensesResponseDTO(
        @Schema(description = "Общая сумма всех расходов поездки", example = "12500.50")
        BigDecimal totalAmount,

        @Schema(description = "Общее количество расходов", example = "15")
        Integer totalCount,

        @ArraySchema(
                arraySchema = @Schema(description = "Список расходов"),
                schema = @Schema(implementation = ExpenseResponseDTO.class)
        )
        List<ExpenseResponseDTO> expenses
) {}
