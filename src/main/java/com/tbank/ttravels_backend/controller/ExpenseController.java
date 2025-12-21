package com.tbank.ttravels_backend.controller;


import com.tbank.ttravels_backend.dto.ErrorResponse;
import com.tbank.ttravels_backend.dto.expense_update.ExpenseUpdateRequestDTO;
import com.tbank.ttravels_backend.dto.exspense.ExpenseRequestDTO;
import com.tbank.ttravels_backend.dto.exspense.ExpenseResponseDTO;
import com.tbank.ttravels_backend.dto.exspense.TravelExpensesResponseDTO;
import com.tbank.ttravels_backend.security.UserPrincipal;
import com.tbank.ttravels_backend.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

@Tag(name = "Expense", description = "Управление расходами и их участниками в рамках поездки")
@RestController
@RequestMapping("/travels/{travelId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(
            summary = "Создать трату",
            description = "Создает новую трату внутри поездки.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Трата успешно создана",
                    content = @Content(schema = @Schema(implementation = ExpenseResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные для создания траты:" +
                    " пустые или отрицательные доли, плательщик не среди участников, пустой DTO",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Пользователь не является участником поездки",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Поездка, трата, плательщик или категория не найдены",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id) && @travelSecurity.isTravelOpen(#travelId)")
    public ExpenseResponseDTO createExpense(
            @Parameter(description = "Данные для создания траты")
            @RequestBody @Valid ExpenseRequestDTO expenseRequestDTO,
            @Parameter(description = "ID поездки", example = "8")
            @PathVariable Long travelId,
            @AuthenticationPrincipal UserPrincipal principal) {

        return expenseService.createExpense(expenseRequestDTO, travelId);
    }


    @Operation(summary = "Удалить трату",
            description = "Удаляет указанную трату из поездки.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Трата успешно удалена"),
            @ApiResponse(responseCode = "403", description = "Пользователь не является участником поездки",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Поездка или трата не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{expenseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id) && @travelSecurity.isTravelOpen(#travelId)")
    public void deleteExpense(
            @Parameter(description = "ID поездки", example = "8")
            @PathVariable Long travelId,
            @Parameter(description = "ID траты", example = "20")
            @PathVariable Long expenseId,
            @AuthenticationPrincipal UserPrincipal principal) {

        expenseService.deleteExpense(travelId, expenseId);
    }


    @Operation(summary = "Обновить трату",
            description = "Обновляет данные существующей траты.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Трата успешно обновлена",
                    content = @Content(schema = @Schema(implementation = ExpenseResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос или пустые доли участников",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Пользователь, данные которого хотят обновить," +
                    " не является участником поездки",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Поездка, категория, плательщик или участник не найдены",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{expenseId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id) && @travelSecurity.isTravelOpen(#travelId)")
    public ExpenseResponseDTO updateExpense(
            @Parameter(description = "ID поездки", example = "8")
            @PathVariable Long travelId,
            @Parameter(description = "ID траты", example = "20")
            @PathVariable Long expenseId,
            @Parameter(description = "Данные для обновления траты")
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "DTO для обновления траты",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ExpenseUpdateRequestDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "Обед с друзьями",
                                      "description": "Поход в кафе после работы",
                                      "date": "2025-11-22T15:30:00+03:00",
                                      "categoryId": 2,
                                      "payerId": 5,
                                      "participantShares": {
                                        "1": 100.0,
                                        "2": 50.0
                                      }
                                    }
                                    """)
                    )
            )
            @RequestBody @Valid ExpenseUpdateRequestDTO expenseUpdateRequestDTO,
            @AuthenticationPrincipal UserPrincipal principal) {

        return expenseService.updateExpense(travelId, expenseId, expenseUpdateRequestDTO);
    }


    @Operation(summary = "Удалить участников из траты",
            description = "Удаляет указанных участников из траты.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Участники успешно удалены",
                    content = @Content(schema = @Schema(implementation = ExpenseResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос на удаление участников:\n" +
                    "- список участников пуст\n" +
                    "- попытка удалить участника, которого нет в трате\n" +
                    "- попытка удалить плательщика",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Пользователь не является участником поездки",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Поездка или трата не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{expenseId}/participants")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id) && @travelSecurity.isTravelOpen(#travelId)")
    public ExpenseResponseDTO removeParticipantsFromExpense(
            @Parameter(description = "ID поездки", example = "8")
            @PathVariable Long travelId,
            @Parameter(description = "ID траты", example = "20")
            @PathVariable Long expenseId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Набор ID участников для удаления",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "[1, 2, 3]")
                    )
            )
            @RequestBody Set<Long> participantsId,
            @AuthenticationPrincipal UserPrincipal principal) {

        return expenseService.deleteParticipantsFromExpense(travelId, expenseId, participantsId);
    }


    @Operation(summary = "Добавить участников к трате",
            description = "Добавляет новых участников к существующей трате.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Участники успешно добавлены",
                    content = @Content(schema = @Schema(implementation = ExpenseResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный список участников для добавления:\n" +
                    "- пустой список участников\n" +
                    "- доли участников пустые или ≤0 ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Пользователь не является участником поездки",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Поездка или трата не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Участник уже есть в этой трате",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{expenseId}/participants")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id) && @travelSecurity.isTravelOpen(#travelId)")
    public ExpenseResponseDTO addParticipantsToExpense(
            @Parameter(description = "ID поездки", example = "8")
            @PathVariable Long travelId,
            @Parameter(description = "ID траты", example = "20")
            @PathVariable Long expenseId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Карта участников и их долей (userId:сумма)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{ \"1\": 100.0, \"2\": 50.0 }"
                            )
                    )
            )
            @RequestBody Map<Long, BigDecimal> participantShares,
            @AuthenticationPrincipal UserPrincipal principal) {

        return expenseService.addParticipantsToExpense(travelId, expenseId, participantShares);
    }


    @Operation(
            summary = "Получить все расходы поездки",
            description = "Возвращает список всех расходов указанной поездки, их общую сумму и количество расходов.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список расходов успешно получен",
                    content = @Content(
                            schema = @Schema(implementation = TravelExpensesResponseDTO.class)
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
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id)")
    public TravelExpensesResponseDTO getTravelExpenses(@Parameter(description = "ID поездки", example = "8")
                                                       @PathVariable Long travelId,
                                                       @AuthenticationPrincipal UserPrincipal principal) {

        return expenseService.getAllExpensesInTravel(travelId);
    }
}
