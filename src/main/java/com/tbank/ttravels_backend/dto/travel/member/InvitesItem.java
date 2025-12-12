package com.tbank.ttravels_backend.dto.travel.member;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Schema(description = "Элемент списка приглашений в поездки")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvitesItem {
    @Schema(description = "Идентификатор приглашения", example = "123")
    private Long inviteId;
    @Schema(description = "Идентификатор поездки", example = "42")
    private String travelName;
    @Schema(description = "Описание поездки", example = "Поездка на море")
    private String description;
    @Schema(description = "Дата и время начала поездки в формате ISO 8601", example = "2024-07-01T10:00:00+03:00")
    private OffsetDateTime startDate;
    @Schema(description = "Дата и время окончания поездки в формате ISO 8601", example = "2024-07-10T18:00:00+03:00")
    private OffsetDateTime endDate;
}
