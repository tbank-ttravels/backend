package com.tbank.ttravels_backend.controller;


import com.tbank.ttravels_backend.dto.exspense.ExpenseRequestDTO;
import com.tbank.ttravels_backend.dto.exspense.ExpenseResponseDTO;
import com.tbank.ttravels_backend.security.UserPrincipal;
import com.tbank.ttravels_backend.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }


    @PostMapping("/travels/{travelId}/expenses")
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponseDTO createExpense(@RequestBody @Valid ExpenseRequestDTO expenseRequestDTO,
                                            @PathVariable Long travelId,
                                            @AuthenticationPrincipal UserPrincipal user) {

        return expenseService.createExpense(expenseRequestDTO, travelId, user.getId());
    }


    @DeleteMapping("/travels/{travelId}/expenses/{expenseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(@PathVariable Long travelId, @PathVariable Long expenseId) {
        expenseService.deleteExpense(travelId, expenseId);
    }
}
