package com.tbank.ttravels_backend.controller;

import com.tbank.ttravels_backend.dto.transfer.CreateTransferRequest;
import com.tbank.ttravels_backend.dto.transfer.EditTransferRequest;
import com.tbank.ttravels_backend.dto.transfer.TransferResponse;
import com.tbank.ttravels_backend.dto.transfer.TransfersListResponse;
import com.tbank.ttravels_backend.security.UserPrincipal;
import com.tbank.ttravels_backend.security.TravelSecurityService;
import com.tbank.ttravels_backend.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;
    private final TravelSecurityService travelSecurity;

    @PostMapping
    @PreAuthorize("@travelSecurity.isMember(#request.travelId, #user.id)")
    public TransferResponse create(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody CreateTransferRequest request
    ) {
        return transferService.createTransfer(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@travelSecurity.isMember(@transferRepository.findById(#id).get().travel.id, #user.id)")
    public TransferResponse edit(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long id,
            @RequestBody EditTransferRequest request
    ) {
        return transferService.editTransfer(id, request);
    }

    @GetMapping("/{travelId}/transfers")
    @PreAuthorize("@travelSecurity.isMember(#travelId, #user.id)")
    public TransfersListResponse getTransfers(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long travelId
    ) {
        return transferService.getTransfersByTravel(travelId);
    }
}
