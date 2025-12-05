package com.tbank.ttravels_backend.service;

import com.tbank.ttravels_backend.dto.exspense.ExpenseResponseDTO;
import com.tbank.ttravels_backend.dto.exspense.TravelExpensesResponseDTO;
import com.tbank.ttravels_backend.entity.Expense;
import com.tbank.ttravels_backend.exception.TravelNotFoundException;
import com.tbank.ttravels_backend.mapper.ExpenseDtoMapper;
import com.tbank.ttravels_backend.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ExpenseServiceGetAllExpensesTest {
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


    @DisplayName("Успешное получение всех трат путешествия")
    @Test
    void getAllExpensesInTravel() {

        // === Given ===
        Long travelId = 1L;

        Expense ex1 = TestDataFactory.fullExpense(1L);
        Expense ex2 = TestDataFactory.fullExpense(2L);
        List<Expense> expenses = List.of(ex1, ex2);
        BigDecimal totalAmount = expenses.stream().map(Expense::getSum).reduce(BigDecimal.ZERO, BigDecimal::add);


        // === Mocking ===
        doNothing().when(travelService).checkTravel(travelId);
        doReturn(expenses).when(expenseRepository).findAllByTravelIdOrderByDateDesc(travelId);


        // === When ===
        TravelExpensesResponseDTO actual = expenseService.getAllExpensesInTravel(travelId);


        // === Then ===
        assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> assertThat(actual.totalAmount()).isEqualByComparingTo(totalAmount),
                () -> assertThat(actual.totalCount()).isEqualTo(expenses.size()),

                // === Проверка участников и их долей ===
                () -> assertParticipantsShares(expenses, actual)
        );


        // === Verify ===
        verify(travelService).checkTravel(travelId);
        verify(expenseRepository).findAllByTravelIdOrderByDateDesc(travelId);
        verifyNoMoreInteractions(travelService, expenseRepository);
    }

    private void assertParticipantsShares(List<Expense> expectedExpenses, TravelExpensesResponseDTO actual) {

        for (Expense expectedExpense : expectedExpenses) {

            ExpenseResponseDTO dto = actual.expenses().stream()
                    .filter(e -> e.id().equals(expectedExpense.getId()))
                    .findFirst()
                    .orElseThrow();

            Map<Long, BigDecimal> expectedShares = expectedExpense.getMemberExpenses().stream()
                    .collect(Collectors.toMap(
                            me -> me.getParticipant().getId(),
                            me -> me.getShare().abs()
                    ));

            assertThat(dto.participants())
                    .hasSize(expectedExpense.getMemberExpenses().size())
                    .allSatisfy(p -> {
                        assertThat(expectedShares).containsKey(p.userId());
                        assertThat(p.share()).isEqualByComparingTo(expectedShares.get(p.userId()));
                    });
        }
    }


    @DisplayName("Ошибка: путешествие не найдено при запросе всех трат")
    @Test
    void throwTravelNotFoundExceptionIfTravelNotFound3() {

        // === Given ===
        Long travelId = 1L;


        // === Mocking ===
        doThrow(TravelNotFoundException.class).when(travelService).checkTravel(travelId);


        // === When & Then ===
        assertThrows(TravelNotFoundException.class, () -> expenseService.getAllExpensesInTravel(travelId));


        // === VERIFY ===
        verify(travelService).checkTravel(travelId);
        verifyNoInteractions(expenseRepository);
    }
}
