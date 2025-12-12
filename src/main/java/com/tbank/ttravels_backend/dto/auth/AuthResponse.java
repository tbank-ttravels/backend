package com.tbank.ttravels_backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Ответ с токенами аутентификации")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    @Schema(description = "Токен доступа", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;
    @Schema(description = "Время жизни токена доступа в секундах", example = "3600")
    private long accessTokenExpiresIn;
    @Schema(description = "Токен обновления", example = "dGhpcy1pcz1hLXJlZnJlc2gtdG9rZW4uLi4=...")
    private String refreshToken;
    @Schema(description = "Время жизни токена обновления в секундах", example = "604800")
    private long refreshTokenExpiresIn;
}