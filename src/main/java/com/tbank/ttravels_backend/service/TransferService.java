package com.tbank.ttravels_backend.service;

import com.tbank.ttravels_backend.dto.transfer.CreateTransferRequest;
import com.tbank.ttravels_backend.dto.transfer.EditTransferRequest;
import com.tbank.ttravels_backend.dto.transfer.TransferResponse;
import com.tbank.ttravels_backend.dto.transfer.TransfersListResponse;
import com.tbank.ttravels_backend.dto.transfer.validator.CreateTransferValidator;
import com.tbank.ttravels_backend.dto.transfer.validator.EditTransferValidator;
import com.tbank.ttravels_backend.entity.Transfer;
import com.tbank.ttravels_backend.entity.Travel;
import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.exception.TransferNotFound;
import com.tbank.ttravels_backend.repository.TransferRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final TravelService travelService;
    private final TravelMemberService travelMemberService;

    @Transactional
    public TransferResponse createTransfer(Long travelId, CreateTransferRequest request) {
        CreateTransferValidator.validateCreate(request);

        Travel travel = travelService.findTravel(travelId);

        User sender = travelMemberService.findUserInTravel(request.getSenderId(), travel.getId());
        User recipient = travelMemberService.findUserInTravel(request.getRecipientId(), travel.getId());

        Transfer transfer = Transfer.builder()
                .sender(sender)
                .recipient(recipient)
                .sum(request.getSum())
                .date(OffsetDateTime.now())
                .build();

        travelService.addTransfer(travel, transfer);
        travelService.saveTravel(travel);

        return toResponse(transfer);
    }

    @Transactional
    public TransferResponse editTransfer(Long transferId, EditTransferRequest request) {
        EditTransferValidator.validate(request);

        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new TransferNotFound("Перевод не найден"));

        transfer.setSum(request.getSum());

        Transfer updated = transferRepository.save(transfer);

        return toResponse(updated);
    }

    @Transactional
    public TransfersListResponse getTransfersByTravel(Long travelId) {
        List<Transfer> transfers = transferRepository.findAllByTravel_Id(travelId);

        List<TransferResponse> items = transfers.stream()
                .map(this::toResponse)
                .toList();

        return new TransfersListResponse(items);
    }

    private TransferResponse toResponse(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getTravel().getId(),
                transfer.getSender().getId(),
                transfer.getRecipient().getId(),
                transfer.getSum(),
                transfer.getDate()
        );
    }
}
