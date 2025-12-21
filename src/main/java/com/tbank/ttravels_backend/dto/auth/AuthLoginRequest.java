package com.tbank.ttravels_backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Данные для входа в систему")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthLoginRequest {
    @Schema(description = "Номер телефона пользователя",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "+79261234567")
    @NotBlank(message = "Поле обязательно!")
    private String phone;

    @Schema(description = "Пароль пользователя",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "securePassword123")
    @NotBlank(message = "Поле обязательно!")
    private String password;
}
