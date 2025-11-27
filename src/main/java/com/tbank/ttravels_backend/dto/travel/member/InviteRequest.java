package com.tbank.ttravels_backend.dto.travel.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "Запрос на приглашение участников в поездку")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InviteRequest {
    @Schema(description = "Список номеров телефонов участников для приглашения",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "[\"+79261234567\", \"+79876543210\"]")
    @NotEmpty(message = "Укажите хотя бы один номер телефона!")
    private List<
            @NotNull(message = "Номер телефона не может быть null")
            @Pattern(regexp = "^\\+?7\\d{10}$", message = "Некорректный номер!")
                    String> phones;
}
