package com.tbank.ttravels_backend.dto.travel;

import com.tbank.ttravels_backend.dto.travel.validator.ValidDateRange;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Schema(description = "Данные для создания поездки")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ValidDateRange
public class CreateTravelRequest {
    @Schema(description = "Название поездки",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Поездка в Казань")
    @NotBlank
    private String name;
    @Schema(description = "Описание поездки", example = "Отпуск на берегу Волги")
    private String description;
    @Schema(description = "Дата и время начала поездки в формате ISO 8601",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "2024-07-01T10:00:00+03:00")
    @NotNull
    private OffsetDateTime startDate;
    @Schema(description = "Дата и время окончания поездки в формате ISO 8601",
            example = "2024-07-10T18:00:00+03:00")
    private OffsetDateTime endDate;
}
