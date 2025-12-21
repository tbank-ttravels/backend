package com.tbank.ttravels_backend.dto.travel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tbank.ttravels_backend.enums.TravelStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Schema(description = "Элемент списка моих поездок")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MyTravelItem {
    @Schema(description = "Идентификатор поездки", example = "42")
    private Long id;
    @Schema(description = "Название поездки", example = "Отпуск на море")
    private String name;
    @Schema(description = "Описание поездки", example = "Путешествие на побережье Средиземного моря")
    private String description;
    @Schema(description = "Дата и время начала поездки в формате ISO 8601", example = "2024-07-01T10:00:00+03:00")
    private OffsetDateTime startDate;
    @Schema(description = "Дата и время окончания поездки в формате ISO 8601", example = "2024-07-15T18:00:00+03:00")
    private OffsetDateTime endDate;
    @Schema(description = "Статус поездки", example = "ACTIVE")
    private TravelStatus travelStatus;
}
