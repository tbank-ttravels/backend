package com.tbank.ttravels_backend.controller;

import com.tbank.ttravels_backend.dto.transfer.CreateTransferRequest;
import com.tbank.ttravels_backend.dto.transfer.EditTransferRequest;
import com.tbank.ttravels_backend.dto.transfer.TransferResponse;
import com.tbank.ttravels_backend.dto.transfer.TransfersListResponse;
import com.tbank.ttravels_backend.security.UserPrincipal;
import com.tbank.ttravels_backend.service.TransferService;
import com.tbank.ttravels_backend.service.TravelSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/travels/{travelId}/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;
    private final TravelSecurityService travelSecurity;

    @PostMapping
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id)")
    public TransferResponse create(
            @PathVariable Long travelId,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CreateTransferRequest request
    ) {
        return transferService.createTransfer(travelId, request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id)")
    public TransferResponse edit(
            @PathVariable Long travelId,
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody EditTransferRequest request
    ) {
        return transferService.editTransfer(travelId, id, request);
    }

    @GetMapping()
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id)")
    public TransfersListResponse getTransfers(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long travelId
    ) {
        return transferService.getTransfersByTravel(travelId);
    }
}
