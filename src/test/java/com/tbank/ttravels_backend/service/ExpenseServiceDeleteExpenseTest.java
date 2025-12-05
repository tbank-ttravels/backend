package com.tbank.ttravels_backend.service;

import com.tbank.ttravels_backend.entity.Expense;
import com.tbank.ttravels_backend.entity.Travel;
import com.tbank.ttravels_backend.exception.ExpenseNotFoundInTravelException;
import com.tbank.ttravels_backend.exception.TravelNotFoundException;
import com.tbank.ttravels_backend.mapper.ExpenseDtoMapper;
import com.tbank.ttravels_backend.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ExpenseServiceDeleteExpenseTest {

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


    @DisplayName("Успешное удаление траты")
    @Test
    void deleteExpenseSuccess() {

        // === Given ===
        Long expenseId = 1L;
        Long travelId = 2L;

        Expense expense = TestDataFactory.expense(expenseId);
        Travel travel = TestDataFactory.travel(travelId);


        // === Mocking ===
        doReturn(travel).when(travelService).findTravel(travelId);
        doReturn(Optional.of(expense))
                .when(expenseRepository)
                .findByIdAndTravelId(expenseId, travelId);
        doNothing().when(travelService).removeExpense(travel, expense);


        // === When & Then ===
        expenseService.deleteExpense(travelId, expenseId);


        // === VERIFY ===
        verify(travelService).findTravel(travelId);
        verify(travelService).removeExpense(travel, expense);
        verify(expenseRepository).findByIdAndTravelId(expenseId, travelId);
        verifyNoMoreInteractions(
                travelService, expenseRepository
        );
    }


    @DisplayName("Ошибка: трата не найдена в путешествии (удаление траты)")
    @Test
    void throwExpenseNotFoundInTravelExceptionIfExpenseNotFoundInTravel() {

        // === Given ===
        Long travelId = 1L;
        Long expenseId = 2L;


        // === Mocking ===
        doReturn(Optional.empty()).when(expenseRepository).findByIdAndTravelId(expenseId, travelId);


        // === When & Then ===
        assertThrows(ExpenseNotFoundInTravelException.class,
                () -> expenseService.deleteExpense(travelId, expenseId));


        // === VERIFY ===
        verifyNoInteractions(travelService);
    }


    @DisplayName("Ошибка: путешествие не найдено (удаление траты)")
    @Test
    void throwTravelNotFoundExceptionIfTravelNotFound2() {

        // === Given ===
        Long travelId = 1L;
        Long expenseId = 2L;

        Expense expense = TestDataFactory.expense(expenseId);


        // === Mocking ===
        doReturn(Optional.of(expense))
                .when(expenseRepository)
                .findByIdAndTravelId(expenseId, travelId);

        doThrow(TravelNotFoundException.class)
                .when(travelService)
                .findTravel(travelId);


        // === When & Then ===
        assertThrows(TravelNotFoundException.class, () -> expenseService.deleteExpense(travelId, expenseId));


        // === VERIFY ===
        verify(expenseRepository).findByIdAndTravelId(expenseId, travelId);
        verify(travelService).findTravel(travelId);
        verify(travelService, never()).removeExpense(any(), any());
    }
}
