package com.tbank.ttravels_backend.service.expense;

import com.tbank.ttravels_backend.dto.expense_update.ExpenseUpdateRequestDTO;
import com.tbank.ttravels_backend.dto.exspense.ExpenseResponseDTO;
import com.tbank.ttravels_backend.dto.exspense.MemberExpenseResponseDTO;
import com.tbank.ttravels_backend.entity.Category;
import com.tbank.ttravels_backend.entity.Expense;
import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.exception.EmptyUpdateRequestException;
import com.tbank.ttravels_backend.exception.ExpenseNotFoundInTravelException;
import com.tbank.ttravels_backend.exception.InvalidParticipantShareException;
import com.tbank.ttravels_backend.exception.UserNotFoundExpenseException;
import com.tbank.ttravels_backend.mapper.ExpenseDtoMapper;
import com.tbank.ttravels_backend.repository.ExpenseRepository;
import com.tbank.ttravels_backend.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ExpenseServiceUpdateExpenseTest {

    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private MemberExpenseService memberExpenseService;
    @Mock
    private TravelMemberService travelMemberService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private UserService userService;
    private final ExpenseDtoMapper expenseDtoMapper = new ExpenseDtoMapper();
    @Mock
    private TravelService travelService;
    private ExpenseService expenseService;


    @BeforeEach
    void init() {

        expenseService = new ExpenseService(
                expenseRepository,
                memberExpenseService,
                travelMemberService,
                categoryService,
                userService,
                expenseDtoMapper,
                travelService
        );
    }

    @Test
    void updateExpense_shouldUpdateExpense() {

        // === Given ===
        Long travelId = 1L, expenseId = 2L;

        OffsetDateTime newDate = OffsetDateTime.of(2025, 5, 6,
                4, 32, 43, 0, ZoneOffset.UTC);
        String newExpenseName = "New expense name";
        String newExpenseDesc = "New expense description";
        BigDecimal newValueMe1 = BigDecimal.valueOf(200);
        Category newCategory = Category.builder().id(2L).name("Category").build();

        Expense expense = TestDataFactory.fullExpense(expenseId);

        Map.Entry<User, BigDecimal> participantEntry = expense.getMemberExpenses().stream()
                .filter(me -> !me.getParticipant().getId().equals(expense.getPayer().getId()))
                .findFirst()
                .map(me -> Map.entry(me.getParticipant(), me.getShare()))
                .orElseThrow(() -> new IllegalStateException("Нет участника кроме плательщика"));

        User participant = participantEntry.getKey();
        BigDecimal valueMe2 = participantEntry.getValue();
        User payer = expense.getPayer();
        Long me2Id = participant.getId();

        ExpenseUpdateRequestDTO requestDTO = TestDataFactory.expenseUpdateRequestDTO(me2Id, newDate,
                newExpenseName, newExpenseDesc, newCategory.getId(), Map.of(payer.getId(), newValueMe1));


        // === Mocking ===
        doReturn(Optional.of(expense)).when(expenseRepository).findByIdAndTravelId(expenseId, travelId);
        doReturn(participant).when(travelMemberService).findUserInTravel(me2Id, travelId);
        doReturn(newCategory).when(categoryService).findCategory(newCategory.getId());
        doReturn(expense).when(expenseRepository).save(any(Expense.class));


        // === When ===
        ExpenseResponseDTO actual = expenseService.updateExpense(travelId, expenseId, requestDTO);


        // === Then ===
        assertAll(
                () -> assertDtoFields(actual, participant, newExpenseName, newExpenseDesc, newValueMe1, valueMe2, newDate, newCategory),

                () -> assertParticipantShares(actual, payer, participant, newValueMe1, valueMe2),

                () -> assertMemberExpenseSigns(expense, participant)
        );


        // === VERIFY ===
        verify(expenseRepository).findByIdAndTravelId(expenseId, travelId);
        verify(travelMemberService).findUserInTravel(me2Id, travelId);
        verify(categoryService).findCategory(newCategory.getId());
        verify(expenseRepository).save(any(Expense.class));
        verifyNoMoreInteractions(expenseRepository, travelMemberService, categoryService);
    }


    @Test
    void updateExpense_shouldThrowWhenUpdateRequestIsEmpty() {

        // === Given ===
        Long travelId = 1L;
        Long expenseId = 2L;
        ExpenseUpdateRequestDTO requestDTO = ExpenseUpdateRequestDTO.builder().build();


        // === When & Then ===
        assertThrows(EmptyUpdateRequestException.class,
                () -> expenseService.updateExpense(travelId, expenseId, requestDTO));


        // === VERIFY ===
        verifyNoInteractions(expenseRepository);
    }


    @Test
    void updateExpense_shouldThrowWhenUserNotFoundInExpense() {

        // === Given ===
        Long travelId = 1L;
        Long expenseId = 2L;

        Expense expense = TestDataFactory.expense(expenseId, 1L, 2L);

        ExpenseUpdateRequestDTO requestDTO = ExpenseUpdateRequestDTO.builder()
                .participantShares(Map.of(99L, BigDecimal.TEN))
                .build();


        // === Mocking ===
        doReturn(Optional.of(expense)).when(expenseRepository).findByIdAndTravelId(expenseId, travelId);


        // === When & Then ===
        assertThrows(UserNotFoundExpenseException.class,
                () -> expenseService.updateExpense(travelId, expenseId, requestDTO));
    }


    @ParameterizedTest
    @NullSource
    @ValueSource(doubles = {-99.0})
    void updateExpense_shouldThrowWhenParticipantShareIsInvalid(Double invalidShare) {

        // === Given ===
        Long travelId = 1L;
        Long expenseId = 2L;
        Long me1Id = 3L;
        Map<Long, BigDecimal> participantShares = new HashMap<>();
        participantShares.put(me1Id, invalidShare == null ? null : BigDecimal.valueOf(invalidShare));

        Expense expense = TestDataFactory.expense(expenseId, me1Id);

        ExpenseUpdateRequestDTO requestDTO = ExpenseUpdateRequestDTO.builder()
                .participantShares(participantShares)
                .build();


        // === Mocking ===
        doReturn(Optional.of(expense)).when(expenseRepository).findByIdAndTravelId(expenseId, travelId);


        // === When & Then ===
        assertThrows(InvalidParticipantShareException.class,
                () -> expenseService.updateExpense(travelId, expenseId, requestDTO));
    }


    @Test
    void updateExpense_shouldThrowWhenExpenseNotFoundInTravel() {

        // === Given ===
        Long travelId = 1L;
        Long expenseId = 2L;
        ExpenseUpdateRequestDTO requestDTO = ExpenseUpdateRequestDTO.builder().categoryId(2L).build();


        // === Mocking ===
        doReturn(Optional.empty()).when(expenseRepository).findByIdAndTravelId(expenseId, travelId);


        // === When & Then ===
        assertThrows(ExpenseNotFoundInTravelException.class,
                () -> expenseService.updateExpense(travelId, expenseId, requestDTO));
    }


    private void assertDtoFields(ExpenseResponseDTO actual, User participant, String newName,
                                 String newDesc, BigDecimal newValueMe1, BigDecimal valueMe2,
                                 OffsetDateTime newDate, Category newCategory) {
        assertThat(actual).isNotNull();
        assertThat(actual.payerId()).isEqualTo(participant.getId());
        assertThat(actual.name()).isEqualTo(newName);
        assertThat(actual.description()).isEqualTo(newDesc);
        assertThat(actual.sum()).isEqualByComparingTo(newValueMe1.abs().add(valueMe2.abs()));
        assertThat(actual.date()).isEqualTo(newDate);
        assertThat(actual.categoryId()).isEqualTo(newCategory.getId());
        assertThat(actual.categoryName()).isEqualTo(newCategory.getName());
    }


    private void assertParticipantShares(ExpenseResponseDTO actual, User payer, User participant,
                                         BigDecimal newValueMe1, BigDecimal valueMe2) {
        for (MemberExpenseResponseDTO p : actual.participants()) {
            if (p.userId().equals(payer.getId())) {
                assertThat(p.share()).isEqualByComparingTo(newValueMe1);
            } else if (p.userId().equals(participant.getId())) {
                assertThat(p.share()).isEqualByComparingTo(valueMe2.abs());
            } else {
                fail("Unexpected participant id: " + p.userId());
            }
        }
    }


    private void assertMemberExpenseSigns(Expense expense, User newPayer) {
        expense.getMemberExpenses().forEach(me -> {
            if (me.getParticipant().getId().equals(newPayer.getId())) {
                assertThat(me.getShare().signum()).isPositive();
            } else {
                assertThat(me.getShare().signum()).isNegative();
            }
        });
    }
}
