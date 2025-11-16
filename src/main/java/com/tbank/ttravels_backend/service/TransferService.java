package com.tbank.ttravels_backend.service;

import com.tbank.ttravels_backend.dto.transfer.*;
import com.tbank.ttravels_backend.entity.Transfer;
import com.tbank.ttravels_backend.entity.Travel;
import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.exception.*;
import com.tbank.ttravels_backend.repository.TransferRepository;
import com.tbank.ttravels_backend.repository.TravelRepository;
import com.tbank.ttravels_backend.repository.UserRepository;
import com.tbank.ttravels_backend.dto.transfer.validator.CreateTransferValidator;
import com.tbank.ttravels_backend.dto.transfer.validator.EditTransferValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final TravelRepository travelRepository;
    private final UserRepository userRepository;

    @Transactional
    public TransferResponse createTransfer(CreateTransferRequest request) {
        CreateTransferValidator.validateCreate(request);

        Travel travel = travelRepository.findById(request.getTravelId())
                .orElseThrow(() -> new TravelNotFound("Поездка не найдена"));

        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new UserNotFound("Отправитель не найден"));

        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new UserNotFound("Получатель не найден"));

        Transfer transfer = Transfer.builder()
                .travel(travel)
                .sender(sender)
                .recipient(recipient)
                .sum(request.getSum())
                .date(OffsetDateTime.now())
                .build();

        Transfer saved = transferRepository.save(transfer);

        return new TransferResponse(
                saved.getId(),
                saved.getTravel().getId(),
                saved.getSender().getId(),
                saved.getRecipient().getId(),
                saved.getSum(),
                saved.getDate()
        );
    }

    @Transactional
    public TransferResponse editTransfer(Long transferId, EditTransferRequest request) {
        EditTransferValidator.validate(request);

        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new TransferNotFound("Перевод не найден"));

        transfer.setSum(request.getSum());

        Transfer updated = transferRepository.save(transfer);

        return new TransferResponse(
                updated.getId(),
                updated.getTravel().getId(),
                updated.getSender().getId(),
                updated.getRecipient().getId(),
                updated.getSum(),
                updated.getDate()
        );
    }

    @Transactional
    public TransfersListResponse getTransfersByTravel(Long travelId) {
        List<Transfer> transfers = transferRepository.findAllByTravel_Id(travelId);

        List<TransferResponse> items = transfers.stream()
                .map(t -> new TransferResponse(
                        t.getId(),
                        t.getTravel().getId(),
                        t.getSender().getId(),
                        t.getRecipient().getId(),
                        t.getSum(),
                        t.getDate()
                ))
                .toList();

        return new TransfersListResponse(items);
    }
}

