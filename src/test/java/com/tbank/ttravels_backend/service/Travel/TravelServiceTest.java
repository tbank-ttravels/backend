package com.tbank.ttravels_backend.service.Travel;

import com.tbank.ttravels_backend.dto.travel.CreateTravelRequest;
import com.tbank.ttravels_backend.dto.travel.EditTravelRequest;
import com.tbank.ttravels_backend.dto.travel.TravelResponse;
import com.tbank.ttravels_backend.entity.*;
import com.tbank.ttravels_backend.enums.MemberRole;
import com.tbank.ttravels_backend.enums.MemberStatus;
import com.tbank.ttravels_backend.enums.TravelStatus;
import com.tbank.ttravels_backend.exception.ConflictStateException;
import com.tbank.ttravels_backend.exception.DuplicateExpenseException;
import com.tbank.ttravels_backend.exception.InvalidDateRangeException;
import com.tbank.ttravels_backend.exception.TravelNotFoundException;
import com.tbank.ttravels_backend.mapper.TravelMapper;
import com.tbank.ttravels_backend.repository.TravelRepository;
import com.tbank.ttravels_backend.service.AccountService;
import com.tbank.ttravels_backend.service.TestDataFactory;
import com.tbank.ttravels_backend.service.TravelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TravelServiceTest {

    @Mock
    private TravelRepository travelRepository;
    @Mock
    private TravelMapper travelMapper;
    @Mock
    private AccountService accountService;
    @InjectMocks
    private TravelService travelService;
    @Captor
    private ArgumentCaptor<Travel> travelCaptor;


    @Test
    void createTravel_shouldCreateOwnerMemberAndMapResponse() {

        long ownerId = 1;
        CreateTravelRequest request = TestDataFactory.createTravelRequest("Поездка", "Описание");
        User owner = TestDataFactory.user(ownerId);
        TravelResponse expectedResponse = new TravelResponse(99L, request.getName(), request.getDescription(),
                request.getStartDate(), request.getEndDate(), TravelStatus.ACTIVE);

        when(accountService.findUser(ownerId)).thenReturn(owner);
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> {
            Travel travel = invocation.getArgument(0);
            travel.setId(99L);
            return travel;
        });
        when(travelMapper.toTravelResponse(any(Travel.class))).thenReturn(expectedResponse);

        TravelResponse response = travelService.createTravel(request, ownerId);

        assertThat(response).isEqualTo(expectedResponse);
        verify(accountService).findUser(ownerId);
        verify(travelRepository).save(travelCaptor.capture());
        Travel saved = travelCaptor.getValue();
        assertThat(saved.getOwner()).isEqualTo(owner);
        assertThat(saved.getStatus()).isEqualTo(TravelStatus.ACTIVE);
        assertThat(saved.getTravelMembers())
                .hasSize(1)
                .allSatisfy(member -> {
                    assertThat(member.getRole()).isEqualTo(MemberRole.OWNER);
                    assertThat(member.getStatus()).isEqualTo(MemberStatus.ACCEPTED);
                    assertThat(member.getUser()).isEqualTo(owner);
                    assertThat(member.getTravel()).isEqualTo(saved);
                });
    }

    @Test
    void getTravel_shouldReturnMappedTravel() {

        long travelId = 10L;
        User owner = TestDataFactory.user(1L);
        Travel existingTravel = TestDataFactory.travel(travelId, owner);
        TravelResponse mapped = new TravelResponse(travelId,
                existingTravel.getName(), existingTravel.getDescription(),
                existingTravel.getStartDate(), existingTravel.getEndDate(), TravelStatus.ACTIVE);

        when(travelRepository.findById(travelId)).thenReturn(Optional.of(existingTravel));
        when(travelMapper.toTravelResponse(existingTravel)).thenReturn(mapped);

        TravelResponse response = travelService.getTravel(travelId);

        assertThat(response).isEqualTo(mapped);
        verify(travelRepository).findById(travelId);
        verify(travelMapper).toTravelResponse(existingTravel);
    }

    @Test
    void getTravel_shouldThrowWhenNotFound() {

        long travelId = 10;

        when(travelRepository.findById(travelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> travelService.getTravel(travelId))
                .isInstanceOf(TravelNotFoundException.class)
                .hasMessageContaining(String.valueOf(travelId));
    }

    @Test
    void editTravel_shouldUpdateNonNullFieldsAndMap() {

        long travelId = 10;
        Travel existingTravel = TestDataFactory.fullTravel(travelId);
        String newTravelName = "new travel name";
        OffsetDateTime newEndDate = existingTravel.getEndDate().plusDays(2);
        EditTravelRequest request = TestDataFactory
                .editTravelRequest(newTravelName, null, null, newEndDate);


        when(travelRepository.findById(travelId)).thenReturn(Optional.of(existingTravel));
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        TravelResponse mapped = new TravelResponse(travelId, newTravelName, existingTravel.getDescription(),
                existingTravel.getStartDate(), newEndDate, TravelStatus.ACTIVE);

        when(travelMapper.toTravelResponse(any(Travel.class))).thenReturn(mapped);

        TravelResponse response = travelService.editTravel(10L, request);

        assertThat(response).isEqualTo(mapped);
        verify(travelRepository).save(travelCaptor.capture());
        Travel saved = travelCaptor.getValue();
        assertThat(saved.getName()).isEqualTo(newTravelName);
        assertThat(saved.getDescription()).isEqualTo(existingTravel.getDescription());
        assertThat(saved.getEndDate()).isEqualTo(newEndDate);
        assertThat(saved.getStartDate()).isEqualTo(existingTravel.getStartDate());
    }

    @Test
    void editTravel_shouldThrowWhenInvalidDateRange() {

        long travelId = 10;
        Travel existingTravel = TestDataFactory.fullTravel(travelId);
        EditTravelRequest request = TestDataFactory.editTravelRequest(null,
                null,
                null,
                existingTravel.getStartDate().minusDays(1));
        when(travelRepository.findById(10L)).thenReturn(Optional.of(existingTravel));

        assertThatThrownBy(() -> travelService.editTravel(10L, request))
                .isInstanceOf(InvalidDateRangeException.class);

        verify(travelRepository, never()).save(any());
    }

    @Test
    void editTravel_shouldThrowWhenNotFound() {
        long travelId = 1;
        when(travelRepository.findById(travelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> travelService.editTravel(travelId, new EditTravelRequest()))
                .isInstanceOf(TravelNotFoundException.class);

        verifyNoMoreInteractions(travelRepository);
    }

    @Test
    void closeTravel_shouldSetStatusClosed() {
        long travelId = 1;
        Travel existingTravel = TestDataFactory.fullTravel(travelId);
        when(travelRepository.findById(travelId)).thenReturn(Optional.of(existingTravel));

        travelService.closeTravel(travelId);

        assertThat(existingTravel.getStatus()).isEqualTo(TravelStatus.CLOSED);
        verify(travelRepository).save(existingTravel);
    }

    @Test
    void closeTravel_shouldThrowWhenAlreadyClosed() {
        long travelId = 1;
        Travel existingTravel = TestDataFactory.travel(travelId, TravelStatus.CLOSED);
        when(travelRepository.findById(10L)).thenReturn(Optional.of(existingTravel));

        assertThatThrownBy(() -> travelService.closeTravel(10L))
                .isInstanceOf(ConflictStateException.class);

        verify(travelRepository, never()).save(any());
    }

    @Test
    void closeTravel_shouldThrowWhenNotFound() {
        long travelId = 1;
        when(travelRepository.findById(travelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> travelService.closeTravel(travelId))
                .isInstanceOf(TravelNotFoundException.class);
    }

    @Test
    void reopenTravel_shouldSetStatusActive() {
        long travelId = 1;
        Travel existingTravel = TestDataFactory.travel(travelId, TravelStatus.CLOSED);
        when(travelRepository.findById(travelId)).thenReturn(Optional.of(existingTravel));

        travelService.reopenTravel(travelId);

        assertThat(existingTravel.getStatus()).isEqualTo(TravelStatus.ACTIVE);
        verify(travelRepository).save(existingTravel);
    }

    @Test
    void reopenTravel_shouldThrowWhenAlreadyActive() {
        long travelId = 1;
        Travel existingTravel = TestDataFactory.fullTravel(travelId);
        when(travelRepository.findById(10L)).thenReturn(Optional.of(existingTravel));

        assertThatThrownBy(() -> travelService.reopenTravel(10L))
                .isInstanceOf(ConflictStateException.class);

        verify(travelRepository, never()).save(any());
    }

    @Test
    void reopenTravel_shouldThrowWhenNotFound() {
        long travelId = 1;
        when(travelRepository.findById(travelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> travelService.reopenTravel(travelId))
                .isInstanceOf(TravelNotFoundException.class);
    }

    @Test
    void deleteTravel_shouldDelete() {

        long travelId = 1;
        Travel existingTravel = TestDataFactory.fullTravel(travelId);
        when(travelRepository.findById(travelId)).thenReturn(Optional.of(existingTravel));

        travelService.deleteTravel(travelId);

        verify(travelRepository).delete(existingTravel);
    }

    @Test
    void deleteTravel_shouldThrowWhenNotFound() {
        long travelId = 1;
        when(travelRepository.findById(travelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> travelService.deleteTravel(travelId))
                .isInstanceOf(TravelNotFoundException.class);

        verify(travelRepository, never()).delete(any());
    }

    @Test
    void addExpense_shouldAttachExpense() {

        long travelId = 1;
        Travel existingTravel = TestDataFactory.fullTravel(travelId);
        Expense expense = new Expense();

        travelService.addExpense(existingTravel, expense);

        assertThat(existingTravel.getExpenses()).contains(expense);
        assertThat(expense.getTravel()).isEqualTo(existingTravel);
    }

    @Test
    void addExpense_shouldThrowOnNulls() {

        long travelId = 1;
        Travel existingTravel = TestDataFactory.fullTravel(travelId);
        Expense expense = new Expense();

        assertThatThrownBy(() -> travelService.addExpense(null, expense))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> travelService.addExpense(existingTravel, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addExpense_shouldThrowOnDuplicate() {

        long travelId = 1;
        Travel existingTravel = TestDataFactory.fullTravel(travelId);
        Expense expense = new Expense();
        existingTravel.getExpenses().add(expense);

        assertThatThrownBy(() -> travelService.addExpense(existingTravel, expense))
                .isInstanceOf(DuplicateExpenseException.class);
    }

    @Test
    void removeExpense_shouldDetachExpense() {

        long travelId = 1;
        Travel existingTravel = TestDataFactory.fullTravel(travelId);
        Expense expense = new Expense();
        expense.setTravel(existingTravel);
        existingTravel.getExpenses().add(expense);

        travelService.removeExpense(existingTravel, expense);

        assertThat(existingTravel.getExpenses()).doesNotContain(expense);
        assertThat(expense.getTravel()).isNull();
    }

    @Test
    void addTransfer_shouldAttachTransfer() {
        long travelId = 1;
        Travel existingTravel = TestDataFactory.fullTravel(travelId);
        Transfer transfer = new Transfer();

        travelService.addTransfer(existingTravel, transfer);

        assertThat(existingTravel.getTransfers()).contains(transfer);
        assertThat(transfer.getTravel()).isEqualTo(existingTravel);
    }

    @Test
    void removeTransfer_shouldDetachTransfer() {
        long travelId = 1;
        Travel existingTravel = TestDataFactory.fullTravel(travelId);
        Transfer transfer = new Transfer();
        transfer.setTravel(existingTravel);
        existingTravel.getTransfers().add(transfer);

        travelService.removeTransfer(existingTravel, transfer);

        assertThat(existingTravel.getTransfers()).doesNotContain(transfer);
        assertThat(transfer.getTravel()).isNull();
    }

    @Test
    void addTransfer_shouldThrowOnNulls() {
        long travelId = 1;
        Travel existingTravel = TestDataFactory.fullTravel(travelId);
        Transfer transfer = new Transfer();

        assertThatThrownBy(() -> travelService.addTransfer(null, transfer))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> travelService.addTransfer(existingTravel, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addTravelMember_shouldAttachMember() {

        long travelId = 1;
        Travel existingTravel = TestDataFactory.fullTravel(travelId);
        TravelMember member = new TravelMember();

        travelService.addTravelMember(existingTravel, member);

        assertThat(existingTravel.getTravelMembers()).contains(member);
        assertThat(member.getTravel()).isEqualTo(existingTravel);
    }

    @Test
    void addTravelMember_shouldThrowOnNulls() {

        long travelId = 1;
        Travel existingTravel = TestDataFactory.fullTravel(travelId);
        TravelMember member = new TravelMember();

        assertThatThrownBy(() -> travelService.addTravelMember(null, member))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> travelService.addTravelMember(existingTravel, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findTravel_shouldReturnWhenExists() {
        long travelId = 1;
        Travel existingTravel = TestDataFactory.fullTravel(travelId);
        when(travelRepository.findById(travelId)).thenReturn(Optional.of(existingTravel));

        Travel result = travelService.findTravel(travelId);

        assertThat(result).isEqualTo(existingTravel);
    }

    @Test
    void findTravel_shouldThrowWhenNotFound() {
        long travelId = 1;
        when(travelRepository.findById(travelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> travelService.findTravel(travelId))
                .isInstanceOf(TravelNotFoundException.class)
                .hasMessageContaining(String.valueOf(travelId));
    }

    @Test
    void checkTravel_shouldThrowWhenNotFound() {
        long travelId = 1;
        when(travelRepository.findById(travelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> travelService.checkTravel(travelId))
                .isInstanceOf(TravelNotFoundException.class);
    }

    @Test
    void checkTravel_shouldReturnWhenExists() {

        long travelId = 1;
        Travel existingTravel = TestDataFactory.fullTravel(travelId);
        when(travelRepository.findById(travelId)).thenReturn(Optional.of(existingTravel));

        travelService.checkTravel(travelId);

        verify(travelRepository).findById(travelId);
    }
}


