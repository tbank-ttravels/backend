package com.tbank.ttravels_backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Ответ с информацией об аккаунте пользователя")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponse {
    @Schema(description = "Номер телефона пользователя", example = "+71234567890")
    private String phone;
    @Schema(description = "Имя пользователя", example = "Иван")
    private String name;
    @Schema(description = "Фамилия пользователя", example = "Иванов")
    private String surname;
}