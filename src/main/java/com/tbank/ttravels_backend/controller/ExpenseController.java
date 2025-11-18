package com.tbank.ttravels_backend.controller;


import com.tbank.ttravels_backend.dto.expense_update.ExpenseUpdateRequestDTO;
import com.tbank.ttravels_backend.dto.exspense.ExpenseRequestDTO;
import com.tbank.ttravels_backend.dto.exspense.ExpenseResponseDTO;
import com.tbank.ttravels_backend.security.UserPrincipal;
import com.tbank.ttravels_backend.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;


@RestController
@RequestMapping("/api/travels/{travelId}/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    // TODO @PreAuthorize("@travelSecurity.isMember(#travelId, user.id)")
    public ExpenseResponseDTO createExpense(@RequestBody @Valid ExpenseRequestDTO expenseRequestDTO,
                                            @PathVariable Long travelId,
                                            @AuthenticationPrincipal UserPrincipal user) {

        return expenseService.createExpense(expenseRequestDTO, travelId);
    }


    // TODO является ли участником поездки?
    @DeleteMapping("/{expenseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // TODO @PreAuthorize("@travelSecurity.isMember(#travelId, user.id)")
    public void deleteExpense(@PathVariable Long travelId,
                              @PathVariable Long expenseId,
                              @AuthenticationPrincipal UserPrincipal user) {

        expenseService.deleteExpense(travelId, expenseId);
    }

    @PatchMapping("/{expenseId}")
    @ResponseStatus(HttpStatus.OK)
    // TODO @PreAuthorize("@travelSecurity.isMember(#travelId, user.id)")
    public ExpenseResponseDTO updateExpense(@PathVariable Long travelId,
                                            @PathVariable Long expenseId,
                                            @RequestBody ExpenseUpdateRequestDTO expenseUpdateRequestDTO,
                                            @AuthenticationPrincipal UserPrincipal user) {

        return expenseService.updateExpense(travelId, expenseId, expenseUpdateRequestDTO);
    }


    @DeleteMapping("/{expenseId}/participants")
    @ResponseStatus(HttpStatus.OK)
    // TODO @PreAuthorize("@travelSecurity.isMember(#travelId, user.id)")
    public ExpenseResponseDTO removeParticipantsFromExpense(@PathVariable Long travelId,
                                                            @PathVariable Long expenseId,
                                                            @RequestBody Set<Long> participantsId,
                                                            @AuthenticationPrincipal UserPrincipal user) {

        return expenseService.deleteParticipantsFromExpense(travelId, expenseId, participantsId);
    }


    // TODO проверка что BigDecimal > 0
    @PostMapping("/{expenseId}/participants")
    @ResponseStatus(HttpStatus.OK)
    // TODO @PreAuthorize("@travelSecurity.isMember(#travelId, user.id)")
    public ExpenseResponseDTO addParticipantsToExpense(@PathVariable Long travelId,
                                                       @PathVariable Long expenseId,
                                                       @RequestBody Map<Long, BigDecimal> participantShares,
                                                       @AuthenticationPrincipal UserPrincipal user) {

        return expenseService.addParticipantsToExpense(travelId, expenseId, participantShares);
    }
}
