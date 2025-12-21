package com.tbank.ttravels_backend.service;

import com.tbank.ttravels_backend.dto.transfer.CreateTransferRequest;
import com.tbank.ttravels_backend.dto.transfer.EditTransferRequest;
import com.tbank.ttravels_backend.dto.transfer.TransferResponse;
import com.tbank.ttravels_backend.dto.transfer.TransfersListResponse;
import com.tbank.ttravels_backend.entity.Transfer;
import com.tbank.ttravels_backend.entity.Travel;
import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.exception.TravelNotFoundException;
import com.tbank.ttravels_backend.exception.UserNotFoundInTravelException;
import com.tbank.ttravels_backend.repository.TransferRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private TravelService travelService;

    @Mock
    private TravelMemberService travelMemberService;

    @InjectMocks
    private TransferService transferService;


    @Test
    void createTransfer() {

        // === Given ===
        long travelId = 1, senderId = 2, recipientId = 3;
        Travel travel = TestDataFactory.travel(travelId);
        User sender = TestDataFactory.user(senderId),
                recipient = TestDataFactory.user(recipientId);
        CreateTransferRequest createTransferRequest = TestDataFactory.createTransferRequest(senderId,
                recipientId, BigDecimal.valueOf(1000));


        // === Mocking ===
        doReturn(travel).when(travelService).findTravel(travelId);
        doReturn(sender).when(travelMemberService).findUserInTravel(senderId, travelId);
        doReturn(recipient).when(travelMemberService).findUserInTravel(recipientId, travelId);
        doAnswer(invocation -> {
            Travel travel1 = invocation.getArgument(0);
            Transfer transfer = invocation.getArgument(1);
            travel1.getTransfers().add(transfer);
            transfer.setTravel(travel1);
            return null;
        }).when(travelService).addTransfer(any(Travel.class), any(Transfer.class));
        doAnswer(invocation -> invocation.getArgument(0)).when(transferRepository).saveAndFlush(any(Transfer.class));

        // === When ===
        TransferResponse actual = transferService.createTransfer(travelId, createTransferRequest);


        // === Then ===
        assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> assertThat(actual.getTravelId()).isEqualTo(travelId),
                () -> assertThat(actual.getSum()).isEqualByComparingTo(createTransferRequest.getSum()),
                () -> assertThat(actual.getRecipientId()).isEqualTo(recipientId),
                () -> assertThat(actual.getSenderId()).isEqualTo(senderId),
                () -> assertThat(travel.getTransfers())
                        .hasSize(1)
                        .allSatisfy(t -> {
                            assertThat(t.getSender().getId()).isEqualTo(senderId);
                            assertThat(t.getRecipient().getId()).isEqualTo(recipientId);
                            assertThat(t.getTravel().getId()).isEqualTo(travelId);
                            assertThat(t.getSum()).isEqualByComparingTo(createTransferRequest.getSum());
                        })
        );

        // === VERIFY ===
        verify(travelService).findTravel(travelId);
        verify(travelMemberService).findUserInTravel(senderId, travelId);
        verify(travelMemberService).findUserInTravel(recipientId, travelId);
        verify(travelService).addTransfer(eq(travel), any(Transfer.class));
        verify(transferRepository).saveAndFlush(any(Transfer.class));
        verifyNoMoreInteractions(travelService, travelMemberService, transferRepository);
    }


    @Test
    void editTransfer() {

        // === Given ===
        long travelId = 1L, transferId = 2L;
        long senderId = 3L, recipientId = 4L;

        User sender = TestDataFactory.user(senderId);
        User recipient = TestDataFactory.user(recipientId);

        BigDecimal oldSum = BigDecimal.valueOf(500);
        BigDecimal newSum = BigDecimal.valueOf(1000);

        Travel travel = TestDataFactory.travel(travelId);
        Transfer transfer = TestDataFactory.transfer(transferId, sender, recipient, oldSum.doubleValue());
        transfer.setTravel(travel);

        EditTransferRequest editTransferRequest = TestDataFactory.editTransferRequest(newSum);


        // === Mocking ===
        doReturn(Optional.of(transfer)).when(transferRepository).findByIdAndTravel_Id(transferId, travelId);
        doAnswer(invocation -> invocation.getArgument(0)).when(transferRepository).save(any(Transfer.class));


        // === When ===
        TransferResponse actual = transferService.editTransfer(travelId, transferId, editTransferRequest);


        // === Then ===
        assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> assertThat(actual.getId()).isEqualTo(transferId),
                () -> assertThat(actual.getTravelId()).isEqualTo(travelId),
                () -> assertThat(actual.getSenderId()).isEqualTo(senderId),
                () -> assertThat(actual.getRecipientId()).isEqualTo(recipientId),
                () -> assertThat(actual.getSum()).isEqualByComparingTo(newSum)
        );


        // === VERIFY ===
        verify(transferRepository).findByIdAndTravel_Id(transferId, travelId);
        verify(transferRepository).save(transfer);
        verifyNoMoreInteractions(transferRepository);
    }


    @Test
    void throwTravelNotFoundException() {

        // === Given ===
        long travelId = 1, senderId = 3L, recipientId = 4L;
        CreateTransferRequest createTransferRequest = TestDataFactory.createTransferRequest(senderId,
                recipientId, BigDecimal.valueOf(1000));

        // === Mocking ===
        doThrow(TravelNotFoundException.class).when(travelService).findTravel(travelId);

        // === When & Then ===
        assertThrows(TravelNotFoundException.class,
                () -> transferService.createTransfer(travelId, createTransferRequest));

        // === VERIFY ===
        verify(travelService).findTravel(travelId);
        verifyNoInteractions(travelMemberService, transferRepository);
    }

    @Test
    void throwSenderNotFoundInTravelException() {

        // === Given ===
        long travelId = 1, senderId = 3L, recipientId = 4L;
        CreateTransferRequest createTransferRequest = TestDataFactory.createTransferRequest(senderId,
                recipientId, BigDecimal.valueOf(1000));
        Travel travel = TestDataFactory.travel(travelId);

        // === Mocking ===
        doThrow(UserNotFoundInTravelException.class).when(travelMemberService).findUserInTravel(senderId, travelId);
        doReturn(travel).when(travelService).findTravel(travelId);

        // === When & Then ===
        assertThrows(UserNotFoundInTravelException.class,
                () -> transferService.createTransfer(travelId, createTransferRequest));


        // === VERIFY ===
        verify(travelService).findTravel(travelId);
        verify(travelMemberService).findUserInTravel(senderId, travelId);
        verifyNoMoreInteractions(travelMemberService);
        verifyNoInteractions(transferRepository);
    }

    @Test
    void throwRecipientNotFoundInTravelException() {

        // === Given ===
        long travelId = 1, senderId = 3L, recipientId = 4L;
        User sender = TestDataFactory.user(senderId);
        CreateTransferRequest createTransferRequest = TestDataFactory.createTransferRequest(senderId,
                recipientId, BigDecimal.valueOf(1000));
        Travel travel = TestDataFactory.travel(travelId);


        // === Mocking ===
        doReturn(travel).when(travelService).findTravel(travelId);
        doReturn(sender).when(travelMemberService).findUserInTravel(senderId, travelId);
        doThrow(UserNotFoundInTravelException.class).when(travelMemberService).findUserInTravel(recipientId, travelId);


        // === When & Then ===
        assertThrows(UserNotFoundInTravelException.class,
                () -> transferService.createTransfer(travelId, createTransferRequest));


        // === VERIFY ===
        verify(travelService).findTravel(travelId);
        verify(travelMemberService).findUserInTravel(senderId, travelId);
        verifyNoMoreInteractions(travelMemberService);
        verifyNoInteractions(transferRepository);
    }


    @Test
    void getTransfersByTravel() {

        // === Given ===
        long travelId = 1;
        Transfer transfer1 = TestDataFactory.transfer(1, 1, 2, 10, 1000);
        Transfer transfer2 = TestDataFactory.transfer(2, 2, 3, 10, 2000);
        Transfer transfer3 = TestDataFactory.transfer(3, 3, 4, 10, 3000);
        List<Transfer> transfers = List.of(transfer1, transfer2, transfer3);


        // === Mocking ===
        doReturn(transfers).when(transferRepository).findAllByTravel_Id(travelId);


        // === When ===
        TransfersListResponse actual = transferService.getTransfersByTravel(travelId);


        // === Then ===
        assertAll("Check all transfers",
                () -> assertThat(actual).isNotNull(),
                () -> assertThat(actual.getTransfers()).hasSize(transfers.size()),
                () -> {
                    for (int i = 0; i < transfers.size(); i++) {
                        assertTransferEqualsResponse(transfers.get(i), actual.getTransfers().get(i));
                    }
                }
        );


        // === VERIFY ===
        verify(transferRepository).findAllByTravel_Id(travelId);
        verifyNoMoreInteractions(transferRepository);
    }

    private void assertTransferEqualsResponse(Transfer expected, TransferResponse actual) {
        assertAll(
                () -> assertThat(actual.getId()).isEqualTo(expected.getId()),
                () -> assertThat(actual.getTravelId()).isEqualTo(expected.getTravel().getId()),
                () -> assertThat(actual.getSenderId()).isEqualTo(expected.getSender().getId()),
                () -> assertThat(actual.getRecipientId()).isEqualTo(expected.getRecipient().getId()),
                () -> assertThat(actual.getSum()).isEqualByComparingTo(expected.getSum()),
                () -> assertThat(actual.getDate()).isEqualTo(expected.getDate())
        );
    }
}
