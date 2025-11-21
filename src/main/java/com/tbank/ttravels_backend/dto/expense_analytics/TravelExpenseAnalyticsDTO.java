package com.tbank.ttravels_backend.dto.expense_analytics;


import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TravelExpenseAnalyticsDTO {

    // Общая сумма всех расходов по поездке
    private BigDecimal totalAmount;

    // Аналитика по категориям
    private List<CategoryAnalyticsResponseDTO> categories;
}
