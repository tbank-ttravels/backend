package com.tbank.ttravels_backend.controller;

import com.tbank.ttravels_backend.dto.ErrorResponse;
import com.tbank.ttravels_backend.dto.travel.CreateTravelRequest;
import com.tbank.ttravels_backend.dto.travel.EditTravelRequest;
import com.tbank.ttravels_backend.dto.travel.MyTravelsResponse;
import com.tbank.ttravels_backend.dto.travel.TravelResponse;
import com.tbank.ttravels_backend.security.UserPrincipal;
import com.tbank.ttravels_backend.service.TravelMemberService;
import com.tbank.ttravels_backend.service.TravelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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

@Tag(name = "Travels", description = "Операции с поездками")
@RestController
@RequestMapping("/api/travels")
@RequiredArgsConstructor
public class TravelController {
    private final TravelService travelService;
    private final TravelMemberService travelMemberService;

    @Operation(
            summary = "Создать новую поездку",
            description = "Создает новую поездку и назначает создателя владельцем",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Поездка успешно создана",
                    content = @Content(schema = @Schema(implementation = TravelResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Неавторизованный доступ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public TravelResponse create(@Valid @RequestBody CreateTravelRequest request,
                                 @AuthenticationPrincipal UserPrincipal principal) {
        return travelService.createTravel(request, principal.getId());
    }

    @Operation(
            summary = "Получить мои поездки",
            description = "Возвращает список всех поездок, в которых участвует пользователь",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список поездок успешно получен",
                    content = @Content(schema = @Schema(implementation = MyTravelsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Неавторизованный доступ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/my")
    @ResponseStatus(HttpStatus.OK)
    public MyTravelsResponse getMyTravels(@AuthenticationPrincipal UserPrincipal principal) {
        return travelMemberService.getMyTravels(principal.getId());
    }

    @Operation(
            summary = "Получить информацию о поездке",
            description = "Доступно только участникам указанной поездки",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Поездка найдена",
                    content = @Content(schema = @Schema(implementation = TravelResponse.class))),
            @ApiResponse(responseCode = "404", description = "Поездка не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа к поездке",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{travelId}")
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id)")
    @ResponseStatus(HttpStatus.OK)
    public TravelResponse getTravel(@Parameter(description = "Идентификатор поездки", required = true, example = "42")
                                    @PathVariable Long travelId,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        return travelService.getTravel(travelId);
    }

    @Operation(
            summary = "Редактировать поездку",
            description = "Позволяет владельцу поездки изменять её детали",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Поездка успешно обновлена",
                    content = @Content(schema = @Schema(implementation = TravelResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Поездка не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа к редактированию поездки",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{travelId}")
    @PreAuthorize("@travelSecurity.isOwner(#travelId, principal.id)")
    @ResponseStatus(HttpStatus.OK)
    public TravelResponse editTravel(@Parameter(description = "Идентификатор поездки", required = true, example = "42")
                                     @PathVariable Long travelId,
                                     @Valid @RequestBody EditTravelRequest request,
                                     @AuthenticationPrincipal UserPrincipal principal) {
        return travelService.editTravel(travelId, request);
    }

    @Operation(
            summary = "Закрыть поездку",
            description = "Позволяет владельцу поездки закрыть её. Закрытая поездка становится доступной только для просмотра.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Поездка успешно закрыта"),
            @ApiResponse(responseCode = "404", description = "Поездка не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа к закрытию поездки",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Поездка уже закрыта",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{travelId}/close")
    @PreAuthorize("@travelSecurity.isOwner(#travelId, principal.id)")
    @ResponseStatus(HttpStatus.OK)
    public void closeTravel(@Parameter(description = "Идентификатор поездки", required = true, example = "42")
                            @PathVariable Long travelId,
                            @AuthenticationPrincipal UserPrincipal principal) {
        travelService.closeTravel(travelId);
    }

    @Operation(
            summary = "Повторно открыть поездку",
            description = "Позволяет владельцу поездки повторно открыть её после закрытия.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Поездка успешно открыта"),
            @ApiResponse(responseCode = "404", description = "Поездка не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа к открытию поездки",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Поездка уже открыта",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{travelId}/reopen")
    @PreAuthorize("@travelSecurity.isOwner(#travelId, principal.id)")
    @ResponseStatus(HttpStatus.OK)
    public void reopenTravel(@Parameter(description = "Идентификатор поездки", required = true, example = "42")
                             @PathVariable Long travelId,
                             @AuthenticationPrincipal UserPrincipal principal) {
        travelService.reopenTravel(travelId);
    }

    @Operation(
            summary = "Удалить поездку",
            description = "Позволяет владельцу поездки удалить её. Все связанные данные также будут удалены.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Поездка успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Поездка не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа к удалению поездки",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{travelId}")
    @PreAuthorize("@travelSecurity.isOwner(#travelId, principal.id)")
    @ResponseStatus(HttpStatus.OK)
    public void deleteTravel(@PathVariable Long travelId,
                             @AuthenticationPrincipal UserPrincipal principal) {
        travelService.deleteTravel(travelId);
    }
}
