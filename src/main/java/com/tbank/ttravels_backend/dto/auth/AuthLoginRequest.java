package com.tbank.ttravels_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthLoginRequest {
    @NotBlank(message = "Поле обязательно!")
    private String phone;

    @NotBlank(message = "Поле обязательно!")
    private String password;
}
