package com.tbank.ttravels_backend.dto.exspense;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class ExpenseRequestDTO {

    @NotBlank(message = "Обязательное поле")
    private String name;

    private String description;

    @Positive(message = "Должно быть > 0")
    private BigDecimal sum;

    private OffsetDateTime date = OffsetDateTime.now();

    private Map<Long, BigDecimal> participantShares = new HashMap<>();

    @NotNull(message = "Обязательное поле")
    private Long categoryId;
}
