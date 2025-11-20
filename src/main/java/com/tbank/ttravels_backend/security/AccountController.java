package com.tbank.ttravels_backend.security;

import com.tbank.ttravels_backend.dto.ErrorResponse;
import com.tbank.ttravels_backend.dto.auth.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Account", description = "Операции управления учетной записью пользователя")
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Изменение пароля пользователя",
            description = "Позволяет аутентифицированному пользователю изменить свой пароль.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204",
                    description = "Пароль успешно изменен"),
            @ApiResponse(responseCode = "401",
                    description = "Некорректные данные для изменения пароля",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @PostMapping("/password/change")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal UserPrincipal principal,
                               @Valid @RequestBody ChangePasswordRequest request) {
        accountService.changePassword(principal.getId(), request);
    }

    @Operation(summary = "Выход пользователя из системы",
            description = "Позволяет аутентифицированному пользователю выйти из системы, аннулируя указанный токен обновления.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204",
                    description = "Пользователь успешно вышел из системы"),
            @ApiResponse(responseCode = "401",
                    description = "Некорректные данные для выхода из системы",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Refresh token не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal UserPrincipal principal,
                       @Valid @RequestBody LogoutRequest request) {
        accountService.logout(principal.getId(), request);
    }

    @Operation(summary = "Обновление токена доступа",
            description = "Позволяет пользователю обновить токен доступа, используя действительный токен обновления."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Токен доступа успешно обновлен",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401",
                    description = "Некорректные данные для обновления токена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Refresh token не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return accountService.refresh(request);
    }

    @Operation(summary = "Получение информации о текущем пользователе",
            description = "Позволяет аутентифицированному пользователю получить информацию о своей учетной записи.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Информация о пользователе успешно получена",
                    content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "401",
                    description = "Пользователь не аутентифицирован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @GetMapping("/me")
    public AccountResponse getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        return accountService.getCurrentUser(principal.getId());
    }
}
