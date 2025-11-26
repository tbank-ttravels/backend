package com.tbank.ttravels_backend.controller;

import com.tbank.ttravels_backend.dto.ErrorResponse;
import com.tbank.ttravels_backend.dto.auth.AuthLoginRequest;
import com.tbank.ttravels_backend.dto.auth.AuthRegisterRequest;
import com.tbank.ttravels_backend.dto.auth.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.tbank.ttravels_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Операции аутентификации пользователей")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Регистрация пользователя",
            description = "Позволяет новому пользователю зарегистрироваться в системе."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Пользователь успешно зарегистрирован",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные для регистрации",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким номером телефона уже существует",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody AuthRegisterRequest request) {
        return authService.register(request);
    }

    @Operation(summary = "Вход пользователя",
            description = "Позволяет зарегистрированному пользователю войти в систему."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Пользователь успешно вошел в систему",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Некорректные учетные данные",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthLoginRequest request) {
        return authService.login(request);
    }
}