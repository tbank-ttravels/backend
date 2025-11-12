package com.tbank.ttravels_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRegisterRequest {
    @NotBlank(message = "Поле телефон обязательно!")
    @Pattern(regexp = "^\\+?7\\d{10}$", message = "Неверный номер!")
    private String phone;

    @NotBlank(message = "Поле имя обязательно!")
    @Size(max = 50, message = "Имя пользователя не может быть больше 50 символов!")
    private String name;

    @NotBlank(message = "Поле фамилия обязательно!")
    @Size(max = 50, message = "Фамилия пользователя не может быть больше 50 символов!")
    private String surname;

    @NotBlank(message = "Поле пароль обязательно!")
    @Size(min = 8, max = 64, message = "Пароль не может быть меньше 8 и больше 64 символов!")
    private String password;
}
