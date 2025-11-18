package com.tbank.ttravels_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
    @NotBlank(message = "Поле обязательно!")
    private String currentPassword;

    @NotBlank(message = "Поле обязательно!")
    @Size(min = 8, max = 64, message = "Не меньше 8 и больше 64 символов!")
    private String newPassword;
}
