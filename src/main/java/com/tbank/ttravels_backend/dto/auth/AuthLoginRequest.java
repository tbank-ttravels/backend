package com.tbank.ttravels_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthLoginRequest {
    @NotBlank(message = "Поле телефон обязательно!")
    private String phone;

    @NotBlank(message = "Поле пароль обязательно!")
    private String password;
}
