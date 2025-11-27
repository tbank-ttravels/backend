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
@Schema(description = "Категория с аналитикой расходов")
public class CategoryAnalyticsResponseDTO {

    // ID категории
    @Schema(description = "ID категории", example = "2")
    private Long id;

    // Название категории
    @Schema(description = "Название категории", example = "Еда")
    private String name;

    // Общая сумма расходов по категории
    @Schema(description = "Общая сумма расходов по категории", example = "500.50")
    private BigDecimal totalAmount;

    // Доля относительно всех расходов по поездке
    @Schema(description = "Доля категории относительно всех расходов по поездке", example = "25.5")
    private BigDecimal percentageOfTotal;

    // Количество отдельных трат
    @Schema(description = "Количество отдельных трат в категории", example = "3")
    private Integer expenseCount;

    // Список участников с их потраченной суммой в этой категории трат
    @ArraySchema(
            arraySchema = @Schema(description = "Список участников с их потраченной суммой в категории"),
            schema = @Schema(implementation = ParticipantStats.class)
    )
    private List<ParticipantStats> participants;

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Статистика участника по категории")
    public static class ParticipantStats  {

        // Имя участника
        @Schema(description = "Имя участника", example = "Иван")
        private String name;

        // Фамилия участника
        @Schema(description = "Фамилия участника", example = "Иванов")
        private String surname;

        // Суммарная сумма, которую этот участник потратил в категории
        @Schema(description = "Сумма расходов участника в категории", example = "200.0")
        private BigDecimal expenseAmount;
    }
}
