package com.tbank.ttravels_backend.controller;

import com.tbank.ttravels_backend.dto.ErrorResponse;
import com.tbank.ttravels_backend.dto.expense_analytics.TravelExpenseAnalyticsDTO;
import com.tbank.ttravels_backend.security.UserPrincipal;
import com.tbank.ttravels_backend.service.ExpenseAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/travels/{travelId}/analytic")
@RequiredArgsConstructor
public class AnalyticExpenseController {
    private final ExpenseAnalyticsService expenseAnalyticsService;


    @Operation(
            summary = "Получить аналитику расходов в поездке по категориям",
            description = "Формирует аналитику расходов по категориям в рамках указанной поездки.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Аналитика успешно сформирована",
                    content = @Content(schema = @Schema(implementation = TravelExpenseAnalyticsDTO.class))
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
    public TravelExpenseAnalyticsDTO getExpenseReport(@Parameter(description = "ID поездки", example = "8")
                                                               @PathVariable Long travelId,
                                                               @AuthenticationPrincipal UserPrincipal principal) {

        return expenseAnalyticsService.generateExpenseReport(travelId);
    }
}
