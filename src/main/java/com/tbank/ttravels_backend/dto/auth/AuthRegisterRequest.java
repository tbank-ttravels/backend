package com.tbank.ttravels_backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Запрос на регистрацию пользователя")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRegisterRequest {
    @Schema(description = "Номер телефона пользователя",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "+79261234567")
    @NotBlank(message = "Поле обязательно!")
    @Pattern(regexp = "^\\+?7\\d{10}$", message = "Неверный номер!")
    private String phone;

    @Schema(description = "Имя пользователя",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Иван")
    @NotBlank(message = "Поле обязательно!")
    @Size(max = 50, message = "Не больше 50 символов!")
    private String name;

    @Schema(description = "Фамилия пользователя",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Иванов")
    @NotBlank(message = "Поле обязательно!")
    @Size(max = 50, message = "Не больше 50 символов!")
    private String surname;

    @Schema(description = "Пароль пользователя",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "securePassword123")
    @NotBlank(message = "Поле обязательно!")
    @Size(min = 8, max = 64, message = "Не меньше 8 и больше 64 символов!")
    private String password;
}
