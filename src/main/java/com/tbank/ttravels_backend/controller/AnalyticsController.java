package com.tbank.ttravels_backend.controller;

import com.tbank.ttravels_backend.dto.expense_analytics.TravelExpenseAnalyticsDTO;
import com.tbank.ttravels_backend.security.UserPrincipal;
import com.tbank.ttravels_backend.service.ExpenseAnalyticsService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/travels/{travelId}/analytic")
public class AnalyticsController {
    private final ExpenseAnalyticsService expenseAnalyticsService;

    public AnalyticsController(ExpenseAnalyticsService expenseAnalyticsService) {
        this.expenseAnalyticsService = expenseAnalyticsService;
    }


    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id)")
    public TravelExpenseAnalyticsDTO analytic(@PathVariable Long travelId,
                                              @AuthenticationPrincipal UserPrincipal principal) {

        return this.expenseAnalyticsService.analyticsCategory(travelId);
    }
}
