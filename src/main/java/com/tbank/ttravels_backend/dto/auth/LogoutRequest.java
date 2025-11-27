package com.tbank.ttravels_backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Данные для выхода из системы")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {
    @Schema(description = "Токен обновления",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "dGhpcy1pcz1hLXJlZnJlc2gtdG9rZW4uLi4=...")
    @NotBlank
    private String refreshToken;
}
