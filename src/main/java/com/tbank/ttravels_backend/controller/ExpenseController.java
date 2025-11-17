package com.tbank.ttravels_backend.controller;


import com.tbank.ttravels_backend.dto.expense_update.ExpenseUpdateRequestDTO;
import com.tbank.ttravels_backend.dto.exspense.ExpenseRequestDTO;
import com.tbank.ttravels_backend.dto.exspense.ExpenseResponseDTO;
import com.tbank.ttravels_backend.security.UserPrincipal;
import com.tbank.ttravels_backend.service.ExpenseService;
import com.tbank.ttravels_backend.service.ReferenceLookupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


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
    public void deleteExpense(@PathVariable Long travelId,
                              @PathVariable Long expenseId,
                              @AuthenticationPrincipal UserPrincipal user) {

        expenseService.deleteExpense(travelId, expenseId, user.getId());
    }

    @PatchMapping("/{expenseId}")
    @ResponseStatus(HttpStatus.OK)
    public ExpenseResponseDTO updateExpense(@PathVariable Long travelId,
                                            @PathVariable Long expenseId,
                                            @RequestBody ExpenseUpdateRequestDTO expenseUpdateRequestDTO,
                                            @AuthenticationPrincipal UserPrincipal user) {

        return expenseService.updateExpense(travelId, expenseId, expenseUpdateRequestDTO, user.getId());
    }

}
