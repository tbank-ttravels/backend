package com.tbank.ttravels_backend.dto.travel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Schema(description = "Запрос на редактирование поездки")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditTravelRequest {
    @Schema(description = "Название поездки", example = "Отпуск на море")
    private String name;
    @Schema(description = "Описание поездки", example = "Поездка на Черное море с друзьями")
    private String description;
    @Schema(description = "Дата и время начала поездки в формате ISO 8601", example = "2024-07-01T10:00:00+03:00")
    private OffsetDateTime startDate;
    @Schema(description = "Дата и время окончания поездки в формате ISO 8601", example = "2024-07-15T18:00:00+03:00")
    private OffsetDateTime endDate;
}
