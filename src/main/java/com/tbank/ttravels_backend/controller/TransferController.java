package com.tbank.ttravels_backend.controller;

import com.tbank.ttravels_backend.dto.transfer.CreateTransferRequest;
import com.tbank.ttravels_backend.dto.transfer.EditTransferRequest;
import com.tbank.ttravels_backend.dto.transfer.TransferResponse;
import com.tbank.ttravels_backend.dto.transfer.TransfersListResponse;
import com.tbank.ttravels_backend.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public TransferResponse create(@RequestBody CreateTransferRequest request) {
        return transferService.createTransfer(request);
    }

    @PutMapping("/{id}")
    public TransferResponse edit(
            @PathVariable Long id,
            @RequestBody EditTransferRequest request) {
        return transferService.editTransfer(id, request);
    }

    @GetMapping("/{travelId}/transfers")
    public TransfersListResponse getTransfers(@PathVariable Long travelId) {
        return transferService.getTransfersByTravel(travelId);
    }
}

