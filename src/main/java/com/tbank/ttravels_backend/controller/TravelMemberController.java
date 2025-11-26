package com.tbank.ttravels_backend.controller;

import com.tbank.ttravels_backend.dto.ErrorResponse;
import com.tbank.ttravels_backend.dto.travel.member.InviteRequest;
import com.tbank.ttravels_backend.dto.travel.member.TravelMembersResponse;
import com.tbank.ttravels_backend.security.UserPrincipal;
import com.tbank.ttravels_backend.service.TravelMemberService;
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

@Tag(name = "Travel Members", description = "Операции управления участниками поездок")
@RestController
@RequestMapping("/travels/{travelId}/members")
@RequiredArgsConstructor
public class TravelMemberController {
    private final TravelMemberService travelMemberService;

    @Operation(summary = "Пригласить участников в поездку",
            description = "Позволяет владельцу или участнику поездки пригласить новых участников по номерам телефонов.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Приглашения успешно отправлены"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Пользователь не является участником поездки",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Поездка не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Один или несколько пользователей уже являются участниками поездки",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/invite")
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id)")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void inviteMember(@Parameter(description = "Идентификатор поездки", required = true, example = "42")
                                 @PathVariable Long travelId,
                             @Valid @RequestBody InviteRequest request,
                             @AuthenticationPrincipal UserPrincipal principal) {
        travelMemberService.inviteMembers(travelId, request.getPhones());
    }

    @Operation(summary = "Получить участников поездки",
            description = "Позволяет получить список всех участников указанной поездки.",
            security = @SecurityRequirement(name = "bearerAuth")

    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список участников успешно получен"),
            @ApiResponse(responseCode = "403", description = "Пользователь не является участником поездки",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Поездка не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping()
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id)")
    @ResponseStatus(HttpStatus.OK)
    public TravelMembersResponse getTravelMembers(@Parameter(description = "Идентификатор поездки", required = true, example = "42")
                                                      @PathVariable Long travelId,
                                                  @AuthenticationPrincipal UserPrincipal principal) {
        return travelMemberService.getTravelMembers(travelId);
    }

    @Operation(summary = "Исключить участника из поездки",
            description = "Позволяет владельцу поездки исключить участника из поездки.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Участник успешно исключён"),
            @ApiResponse(responseCode = "403", description = "Пользователь не является владельцем поездки",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Участник или поездка не найдены",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Попытка исключить владельца поездки",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/kick/{userId}")
    @PreAuthorize("@travelSecurity.isOwner(#travelId, principal.id)")
    @ResponseStatus(HttpStatus.OK)
    public void kickMember(@Parameter(description = "Идентификатор поездки", required = true, example = "42")
                               @PathVariable Long travelId,
                           @Parameter(description = "Идентификатор пользователя", required = true, example = "25")
                               @PathVariable Long userId,
                           @AuthenticationPrincipal UserPrincipal principal) {
        travelMemberService.kickMember(travelId, userId);
    }

    @Operation(summary = "Покинуть поездку",
            description = "Позволяет участнику покинуть поездку.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Участник успешно покинул поездку"),
            @ApiResponse(responseCode = "403", description = "Пользователь не является участником поездки",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Поездка не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Попытка владельца покинуть поездку",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/leave")
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id)")
    @ResponseStatus(HttpStatus.OK)
    public void leaveTravel(@Parameter(description = "Идентификатор поездки", required = true, example = "42")
                                @PathVariable Long travelId,
                            @AuthenticationPrincipal UserPrincipal principal) {
        travelMemberService.leaveTravel(travelId, principal.getId());
    }
}