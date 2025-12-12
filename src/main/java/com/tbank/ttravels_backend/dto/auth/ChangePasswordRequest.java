package com.tbank.ttravels_backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Данные для изменения пароля")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
    @Schema(description = "Текущий пароль пользователя",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "currentPassword123")
    @NotBlank(message = "Поле обязательно!")
    private String currentPassword;

    @Schema(description = "Новый пароль пользователя",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "newSecurePassword456")
    @NotBlank(message = "Поле обязательно!")
    @Size(min = 8, max = 64, message = "Не меньше 8 и больше 64 символов!")
    private String newPassword;
}
