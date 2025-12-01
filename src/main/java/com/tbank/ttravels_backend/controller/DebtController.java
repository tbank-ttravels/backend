package com.tbank.ttravels_backend.controller;

import com.tbank.ttravels_backend.dto.ErrorResponse;
import com.tbank.ttravels_backend.dto.debt.TravelDebtsResponseDTO;
import com.tbank.ttravels_backend.security.UserPrincipal;
import com.tbank.ttravels_backend.service.DebtCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Debt", description = "Расчёт долгов между участниками поездки")
@RestController
@RequestMapping("/travels/{travelId}/debt")
@RequiredArgsConstructor
public class DebtController {

    private final DebtCalculationService debtCalculationService;

    @Operation(
            summary = "Получить долги пользователя",
            description = "Возвращает рассчитанные долги текущего пользователя в рамках указанной поездки.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Долги успешно получены",
                    content = @Content(
                            schema = @Schema(implementation = TravelDebtsResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Пользователь не является участником поездки",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Поездка не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id)")
    public TravelDebtsResponseDTO meth(@Parameter(description = "ID поездки", example = "8")
                                       @PathVariable Long travelId,
                                       @AuthenticationPrincipal UserPrincipal principal) {

        return debtCalculationService.calculateDebtsForUser(principal.getId(), travelId);
    }
}
