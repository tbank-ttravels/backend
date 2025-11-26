package com.tbank.ttravels_backend.dto.expense_analytics;


import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Аналитика расходов по поездке")
public class TravelExpenseAnalyticsDTO {

    // Общая сумма всех расходов по поездке
    @Schema(description = "Общая сумма всех расходов по поездке", example = "2000.0")
    private BigDecimal totalAmount;

    // Аналитика по категориям
    @ArraySchema(
            arraySchema = @Schema(description = "Аналитика по категориям"),
            schema = @Schema(implementation = CategoryAnalyticsResponseDTO.class)
    )
    private List<CategoryAnalyticsResponseDTO> categories;
}
