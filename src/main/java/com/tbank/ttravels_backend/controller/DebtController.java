package com.tbank.ttravels_backend.controller;

import com.tbank.ttravels_backend.dto.debt.TravelDebtsResponse;
import com.tbank.ttravels_backend.security.UserPrincipal;
import com.tbank.ttravels_backend.service.DebtCalculationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Debt", description = "")
@RestController
@RequestMapping("/travels/{travelId}/debt")
@RequiredArgsConstructor
public class DebtController {

    private final DebtCalculationService debtCalculationService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id)")
    public TravelDebtsResponse meth(@PathVariable Long travelId,
                                    @AuthenticationPrincipal UserPrincipal principal) {

        return debtCalculationService.calculateDebtsForUser(principal.getId(), travelId);
    }
}
