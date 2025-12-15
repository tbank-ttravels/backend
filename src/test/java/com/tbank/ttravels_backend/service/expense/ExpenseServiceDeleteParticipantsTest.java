package com.tbank.ttravels_backend.service.expense;

import com.tbank.ttravels_backend.dto.exspense.ExpenseResponseDTO;
import com.tbank.ttravels_backend.dto.exspense.MemberExpenseResponseDTO;
import com.tbank.ttravels_backend.entity.Category;
import com.tbank.ttravels_backend.entity.Expense;
import com.tbank.ttravels_backend.entity.MemberExpense;
import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.exception.CannotRemovePayerFromExpenseException;
import com.tbank.ttravels_backend.exception.EmptyParticipantsListException;
import com.tbank.ttravels_backend.exception.ExpenseNotFoundInTravelException;
import com.tbank.ttravels_backend.exception.UserNotFoundExpenseException;
import com.tbank.ttravels_backend.mapper.ExpenseDtoMapper;
import com.tbank.ttravels_backend.repository.ExpenseRepository;
import com.tbank.ttravels_backend.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ExpenseServiceDeleteParticipantsTest {
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
    void deleteParticipantsFromExpense_shouldRemoveParticipants() {

        // === Given ===
        Long travelId = 1L;
        Long expenseId = 2L;

        User payer = User.builder().id(3L).build(),
                participant1 = User.builder().id(4L).build(),
                participant2 = User.builder().id(5L).build();

        MemberExpense me1 = MemberExpense.builder().participant(payer).share(BigDecimal.valueOf(100)).build(),
                me2 = MemberExpense.builder().participant(participant1).share(BigDecimal.valueOf(-150)).build(),
                me3 = MemberExpense.builder().participant(participant2).share(BigDecimal.valueOf(-250)).build();

        Expense expense = TestDataFactory.expense(
                expenseId,
                payer,
                new HashSet<>(Set.of(me1, me2, me3)),
                Category.builder().id(1L).build());


        // === Mocking ===
        doReturn(Optional.of(expense)).when(expenseRepository).findByIdAndTravelId(expenseId, travelId);

        doAnswer(invocation -> {
            Expense exp = invocation.getArgument(0);
            MemberExpense memberExp = invocation.getArgument(1);
            exp.getMemberExpenses().remove(memberExp);
            return null;
        }).when(memberExpenseService).removeMemberExpense(expense, me3);


        // === When ===
        ExpenseResponseDTO actual =
                expenseService.deleteParticipantsFromExpense(travelId, expenseId, Set.of(participant2.getId()));


        // === Then ===
        assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> assertThat(actual.sum()).isEqualTo(me1.getShare().add(me2.getShare().abs())),

                () -> {
                    assertThat(actual.participants())
                            .hasSize(2)
                            .extracting(MemberExpenseResponseDTO::userId)
                            .containsExactlyInAnyOrder(payer.getId(), participant1.getId());
                }
        );


        // === VERIFY ===
        verify(memberExpenseService).removeMemberExpense(expense, me3);
        verify(expenseRepository).findByIdAndTravelId(expenseId, travelId);
        verifyNoMoreInteractions(memberExpenseService, expenseRepository);
    }


    @Test
    void deleteParticipantsFromExpense_shouldThrowWhenParticipantsListIsEmpty() {

        // === When & Then ===
        assertThrows(EmptyParticipantsListException.class,
                () -> expenseService.deleteParticipantsFromExpense(1L, 2L, Set.of()));

        // === VERIFY ===
        verifyNoInteractions(expenseRepository, memberExpenseService);
    }


    @Test
    void deleteParticipantsFromExpense_shouldThrowWhenExpenseNotFoundInTravel() {

        // === Given ===
        Long travelId = 1L;
        Long expenseId = 2L;


        // === Mocking ===
        doReturn(Optional.empty()).when(expenseRepository).findByIdAndTravelId(expenseId, travelId);


        // === When & Then ===
        assertThrows(ExpenseNotFoundInTravelException.class,
                () -> expenseService.deleteParticipantsFromExpense(travelId, expenseId, Set.of(1L)));


        // === VERIFY ===
        verifyNoInteractions(memberExpenseService);
    }


    @Test
    void deleteParticipantsFromExpense_shouldThrowWhenUserNotFoundInExpense() {

        // === Given ===
        Long travelId = 1L;
        Long expenseId = 2L;

        User payer = User.builder().id(3L).build(),
                participant = User.builder().id(4L).build();

        Expense expense = TestDataFactory.expense(payer,
                TestDataFactory.memberExpense(payer, 100),
                TestDataFactory.memberExpense(participant, -100));


        // === Mocking ===
        doReturn(Optional.of(expense)).when(expenseRepository).findByIdAndTravelId(expenseId, travelId);


        // === When & Then ===
        assertThrows(UserNotFoundExpenseException.class,
                () -> expenseService.deleteParticipantsFromExpense(travelId, expenseId, Set.of(99L)));


        // === VERIFY ===
        verifyNoInteractions(memberExpenseService);
    }


    @Test
    void deleteParticipantsFromExpense_shouldThrowWhenParticipantIsPayer() {

        // === Given ===
        Long travelId = 1L;
        Long expenseId = 2L;

        User payer = User.builder().id(3L).build(),
                participant = User.builder().id(4L).build();

        Expense expense = TestDataFactory.expense(payer,
                TestDataFactory.memberExpense(payer, 100),
                TestDataFactory.memberExpense(participant, -100));


        // === Mocking ===
        doReturn(Optional.of(expense)).when(expenseRepository).findByIdAndTravelId(expenseId, travelId);


        // === When & Then ===
        assertThrows(CannotRemovePayerFromExpenseException.class,
                () -> expenseService.deleteParticipantsFromExpense(travelId, expenseId, Set.of(payer.getId())));


        // === VERIFY ===
        verifyNoInteractions(memberExpenseService);
    }
}
