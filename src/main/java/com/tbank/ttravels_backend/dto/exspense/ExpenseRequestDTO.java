package com.tbank.ttravels_backend.dto.exspense;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@Builder
@Schema(description = "DTO для создания или обновления траты")
public class ExpenseRequestDTO {

    @NotBlank(message = "Обязательное поле")
    @Schema(description = "Название траты", example = "Обед в ресторане")
    private String name;

    @Schema(description = "Описание траты", example = "Совместный обед участников поездки")
    private String description;

    @NotNull(message = "Плательщик не выбран")
    @Schema(description = "ID пользователя, который является плательщиком", example = "5")
    private Long payerId;

    @Builder.Default
    @Schema(description = "Дата и время траты", example = "2025-11-22T14:30:00+03:00")
    private OffsetDateTime date = OffsetDateTime.now();

    @NotEmpty(message = "Отсутствуют участники")
    @Builder.Default
    @Schema(description = "Карта(мапа) участников с их долями в тратах (userId : сумма)",
            example = "{1: 500, 2: 450}")
    private Map<Long, BigDecimal> participantShares = new HashMap<>();

    @NotNull(message = "Категория не выбрана")
    @Schema(description = "ID категории траты", example = "5")
    private Long categoryId;
}