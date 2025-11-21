package com.tbank.ttravels_backend.dto.expense_analytics;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CategoryAnalyticsResponseDTO {

    // ID категории
    private Long id;

    // Название категории
    private String name;

    // Общая сумма расходов по категории
    private BigDecimal totalAmount;

    // Доля относительно всех расходов по поездке
    private BigDecimal percentageOfTotal;

    // Количество отдельных трат
    private Integer expenseCount;

    // Список участников с их потраченной суммой в этой категории трат
    private List<ParticipantStats> participants;

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ParticipantStats  {

        // Имя участника
        private String name;

        // Фамилия участника
        private String surname;

        // Суммарная сумма, которую этот участник потратил в категории
        private BigDecimal expenseAmount;
    }
}
