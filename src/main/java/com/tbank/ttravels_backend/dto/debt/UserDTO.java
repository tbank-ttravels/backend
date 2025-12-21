package com.tbank.ttravels_backend.dto.debt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Информация о пользователе")
@Builder
public record UserDTO(
        @Schema(description = "ID пользователя", example = "8")
        Long id,

        @Schema(description = "Имя пользователя", example = "Иван")
        String name,

        @Schema(description = "Фамилия пользователя", example = "Иванов")
        String surname
) {
}