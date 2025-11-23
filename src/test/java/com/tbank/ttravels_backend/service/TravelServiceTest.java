package com.tbank.ttravels_backend.service;

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
import com.tbank.ttravels_backend.security.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
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

    private User owner;
    private Travel existingTravel;
    private OffsetDateTime start;
    private OffsetDateTime end;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .phone("+78005553535")
                .name("Иван")
                .surname("Иванов")
                .build();

        start = OffsetDateTime.parse("2025-01-10T10:00:00+03:00");
        end = OffsetDateTime.parse("2025-01-20T10:00:00+03:00");

        existingTravel = Travel.builder()
                .id(10L)
                .name("Старое имя")
                .description("Старое описание")
                .startDate(start)
                .endDate(end)
                .owner(owner)
                .status(TravelStatus.ACTIVE)
                .travelMembers(new HashSet<>())
                .expenses(new ArrayList<>())
                .transfers(new ArrayList<>())
                .build();
    }

    @Test
    void createTravel_shouldCreateOwnerMemberAndMapResponse() {
        CreateTravelRequest request = new CreateTravelRequest("Поездка", "Описание", start, end);
        when(accountService.findUser(1L)).thenReturn(owner);
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> {
            Travel travel = invocation.getArgument(0);
            travel.setId(99L);
            return travel;
        });
        TravelResponse expectedResponse = new TravelResponse(99L, "Поездка", "Описание", start, end, TravelStatus.ACTIVE);
        when(travelMapper.toTravelResponse(any(Travel.class))).thenReturn(expectedResponse);

        TravelResponse response = travelService.createTravel(request, 1L);

        assertThat(response).isEqualTo(expectedResponse);
        verify(accountService).findUser(1L);
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
        when(travelRepository.findById(10L)).thenReturn(Optional.of(existingTravel));
        TravelResponse mapped = new TravelResponse(10L, "Старое имя", "Старое описание", start, end, TravelStatus.ACTIVE);
        when(travelMapper.toTravelResponse(existingTravel)).thenReturn(mapped);

        TravelResponse response = travelService.getTravel(10L);

        assertThat(response).isEqualTo(mapped);
        verify(travelRepository).findById(10L);
        verify(travelMapper).toTravelResponse(existingTravel);
    }

    @Test
    void getTravel_shouldThrowWhenNotFound() {
        when(travelRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> travelService.getTravel(10L))
                .isInstanceOf(TravelNotFoundException.class)
                .hasMessageContaining("10");
    }

    @Test
    void editTravel_shouldUpdateNonNullFieldsAndMap() {
        EditTravelRequest request = new EditTravelRequest("Новое имя", null, null, end.plusDays(2));
        when(travelRepository.findById(10L)).thenReturn(Optional.of(existingTravel));
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        TravelResponse mapped = new TravelResponse(10L, "Новое имя", "Старое описание", start, end.plusDays(2), TravelStatus.ACTIVE);
        when(travelMapper.toTravelResponse(any(Travel.class))).thenReturn(mapped);

        TravelResponse response = travelService.editTravel(10L, request);

        assertThat(response).isEqualTo(mapped);
        verify(travelRepository).save(travelCaptor.capture());
        Travel saved = travelCaptor.getValue();
        assertThat(saved.getName()).isEqualTo("Новое имя");
        assertThat(saved.getDescription()).isEqualTo("Старое описание");
        assertThat(saved.getEndDate()).isEqualTo(end.plusDays(2));
        assertThat(saved.getStartDate()).isEqualTo(start);
    }

    @Test
    void editTravel_shouldThrowWhenInvalidDateRange() {
        EditTravelRequest request = new EditTravelRequest(null, null, null, start.minusDays(1));
        when(travelRepository.findById(10L)).thenReturn(Optional.of(existingTravel));

        assertThatThrownBy(() -> travelService.editTravel(10L, request))
                .isInstanceOf(InvalidDateRangeException.class);

        verify(travelRepository, never()).save(any());
    }

    @Test
    void editTravel_shouldThrowWhenNotFound() {
        when(travelRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> travelService.editTravel(10L, new EditTravelRequest()))
                .isInstanceOf(TravelNotFoundException.class);

        verifyNoMoreInteractions(travelRepository);
    }

    @Test
    void closeTravel_shouldSetStatusClosed() {
        existingTravel.setStatus(TravelStatus.ACTIVE);
        when(travelRepository.findById(10L)).thenReturn(Optional.of(existingTravel));

        travelService.closeTravel(10L);

        assertThat(existingTravel.getStatus()).isEqualTo(TravelStatus.CLOSED);
        verify(travelRepository).save(existingTravel);
    }

    @Test
    void closeTravel_shouldThrowWhenAlreadyClosed() {
        existingTravel.setStatus(TravelStatus.CLOSED);
        when(travelRepository.findById(10L)).thenReturn(Optional.of(existingTravel));

        assertThatThrownBy(() -> travelService.closeTravel(10L))
                .isInstanceOf(ConflictStateException.class);

        verify(travelRepository, never()).save(any());
    }

    @Test
    void closeTravel_shouldThrowWhenNotFound() {
        when(travelRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> travelService.closeTravel(10L))
                .isInstanceOf(TravelNotFoundException.class);
    }

    @Test
    void reopenTravel_shouldSetStatusActive() {
        existingTravel.setStatus(TravelStatus.CLOSED);
        when(travelRepository.findById(10L)).thenReturn(Optional.of(existingTravel));

        travelService.reopenTravel(10L);

        assertThat(existingTravel.getStatus()).isEqualTo(TravelStatus.ACTIVE);
        verify(travelRepository).save(existingTravel);
    }

    @Test
    void reopenTravel_shouldThrowWhenAlreadyActive() {
        existingTravel.setStatus(TravelStatus.ACTIVE);
        when(travelRepository.findById(10L)).thenReturn(Optional.of(existingTravel));

        assertThatThrownBy(() -> travelService.reopenTravel(10L))
                .isInstanceOf(ConflictStateException.class);

        verify(travelRepository, never()).save(any());
    }

    @Test
    void reopenTravel_shouldThrowWhenNotFound() {
        when(travelRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> travelService.reopenTravel(10L))
                .isInstanceOf(TravelNotFoundException.class);
    }

    @Test
    void deleteTravel_shouldDelete() {
        when(travelRepository.findById(10L)).thenReturn(Optional.of(existingTravel));

        travelService.deleteTravel(10L);

        verify(travelRepository).delete(existingTravel);
    }

    @Test
    void deleteTravel_shouldThrowWhenNotFound() {
        when(travelRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> travelService.deleteTravel(10L))
                .isInstanceOf(TravelNotFoundException.class);

        verify(travelRepository, never()).delete(any());
    }

    @Test
    void addExpense_shouldAttachExpense() {
        Expense expense = new Expense();
        existingTravel.getExpenses().clear();

        travelService.addExpense(existingTravel, expense);

        assertThat(existingTravel.getExpenses()).contains(expense);
        assertThat(expense.getTravel()).isEqualTo(existingTravel);
    }

    @Test
    void addExpense_shouldThrowOnNulls() {
        Expense expense = new Expense();

        assertThatThrownBy(() -> travelService.addExpense(null, expense))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> travelService.addExpense(existingTravel, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addExpense_shouldThrowOnDuplicate() {
        Expense expense = new Expense();
        existingTravel.getExpenses().add(expense);

        assertThatThrownBy(() -> travelService.addExpense(existingTravel, expense))
                .isInstanceOf(DuplicateExpenseException.class);
    }

    @Test
    void removeExpense_shouldDetachExpense() {
        Expense expense = new Expense();
        expense.setTravel(existingTravel);
        existingTravel.getExpenses().add(expense);

        travelService.removeExpense(existingTravel, expense);

        assertThat(existingTravel.getExpenses()).doesNotContain(expense);
        assertThat(expense.getTravel()).isNull();
    }

    @Test
    void addTransfer_shouldAttachTransfer() {
        Transfer transfer = new Transfer();
        existingTravel.getTransfers().clear();

        travelService.addTransfer(existingTravel, transfer);

        assertThat(existingTravel.getTransfers()).contains(transfer);
        assertThat(transfer.getTravel()).isEqualTo(existingTravel);
    }

    @Test
    void removeTransfer_shouldDetachTransfer() {
        Transfer transfer = new Transfer();
        transfer.setTravel(existingTravel);
        existingTravel.getTransfers().add(transfer);

        travelService.removeTransfer(existingTravel, transfer);

        assertThat(existingTravel.getTransfers()).doesNotContain(transfer);
        assertThat(transfer.getTravel()).isNull();
    }

    @Test
    void addTransfer_shouldThrowOnNulls() {
        Transfer transfer = new Transfer();

        assertThatThrownBy(() -> travelService.addTransfer(null, transfer))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> travelService.addTransfer(existingTravel, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addTravelMember_shouldAttachMember() {
        TravelMember member = new TravelMember();
        existingTravel.getTravelMembers().clear();

        travelService.addTravelMember(existingTravel, member);

        assertThat(existingTravel.getTravelMembers()).contains(member);
        assertThat(member.getTravel()).isEqualTo(existingTravel);
    }

    @Test
    void removeTravelMember_shouldDetachMember() {
        TravelMember member = new TravelMember();
        member.setTravel(existingTravel);
        existingTravel.getTravelMembers().add(member);

        travelService.removeTravelMember(existingTravel, member);

        assertThat(existingTravel.getTravelMembers()).doesNotContain(member);
        assertThat(member.getTravel()).isNull();
    }

    @Test
    void addTravelMember_shouldThrowOnNulls() {
        TravelMember member = new TravelMember();

        assertThatThrownBy(() -> travelService.addTravelMember(null, member))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> travelService.addTravelMember(existingTravel, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findTravel_shouldReturnWhenExists() {
        when(travelRepository.findById(10L)).thenReturn(Optional.of(existingTravel));

        Travel result = travelService.findTravel(10L);

        assertThat(result).isEqualTo(existingTravel);
    }

    @Test
    void findTravel_shouldThrowWhenNotFound() {
        when(travelRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> travelService.findTravel(10L))
                .isInstanceOf(TravelNotFoundException.class)
                .hasMessageContaining("10");
    }

    @Test
    void checkTravel_shouldThrowWhenNotFound() {
        when(travelRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> travelService.checkTravel(10L))
                .isInstanceOf(TravelNotFoundException.class);
    }

    @Test
    void checkTravel_shouldReturnWhenExists() {
        when(travelRepository.findById(10L)).thenReturn(Optional.of(existingTravel));

        travelService.checkTravel(10L);

        verify(travelRepository).findById(10L);
    }
}
