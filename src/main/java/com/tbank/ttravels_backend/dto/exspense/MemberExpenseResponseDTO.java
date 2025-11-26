package com.tbank.ttravels_backend.dto.exspense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Objects;


/**
 * @param userId  ID пользователя
 * @param name    Имя
 * @param surname Фамилия
 * @param share   Доля (+ платил, - должен)
 */


@Builder
public record MemberExpenseResponseDTO(
        @Schema(description = "ID пользователя", example = "45") Long userId,
        @Schema(description = "Имя пользователя", example = "Иван") String name,
        @Schema(description = "Фамилия пользователя", example = "Иванов") String surname,
        @Schema(description = "Доля пользователя", example = "800") BigDecimal share
) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberExpenseResponseDTO that = (MemberExpenseResponseDTO) o;
        return userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
