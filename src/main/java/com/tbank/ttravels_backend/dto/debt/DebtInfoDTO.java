package com.tbank.ttravels_backend.dto.debt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Schema(description = "Информация о долге между пользователями")
public record DebtInfoDTO(

        @Schema(description = "Информация о другом пользователе", implementation = UserDTO.class)
        UserDTO user,

        @Schema(description = "Сумма долга", example = "450.75")
        BigDecimal totalAmount
) {
}