package com.tbank.ttravels_backend.dto.debt;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "Ответ с расчетом долгов пользователя в поездке")
@Builder
public record TravelDebtsResponseDTO(

        @ArraySchema(
                arraySchema = @Schema(description = "Кому пользователь должен"),
                schema = @Schema(implementation = DebtInfoDTO.class)
        )
        List<DebtInfoDTO> debts,

        @ArraySchema(
                arraySchema = @Schema(description = "Кто должен пользователю"),
                schema = @Schema(implementation = DebtInfoDTO.class)
        )
        List<DebtInfoDTO> creditors
) {
}