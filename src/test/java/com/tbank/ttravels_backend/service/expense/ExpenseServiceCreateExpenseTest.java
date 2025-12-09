package com.tbank.ttravels_backend.service.expense;

import com.tbank.ttravels_backend.dto.exspense.ExpenseRequestDTO;
import com.tbank.ttravels_backend.dto.exspense.ExpenseResponseDTO;
import com.tbank.ttravels_backend.dto.exspense.MemberExpenseResponseDTO;
import com.tbank.ttravels_backend.entity.*;
import com.tbank.ttravels_backend.exception.*;
import com.tbank.ttravels_backend.mapper.ExpenseDtoMapper;
import com.tbank.ttravels_backend.repository.ExpenseRepository;
import com.tbank.ttravels_backend.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ExpenseServiceCreateExpenseTest {

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


    @DisplayName("Успешное создание траты")
    @Test
    void createExpenseSuccess() {

        // === Given ===
        Long travelId = 2L;

        Travel travel = TestDataFactory.travel(travelId);
        Category category = TestDataFactory.category(3L);
        User payer = TestDataFactory.user(1L);
        User participant = TestDataFactory.user(2L);

        BigDecimal payerShare = BigDecimal.valueOf(100);
        BigDecimal participantShare = BigDecimal.valueOf(50);

        ExpenseRequestDTO requestDTO = TestDataFactory.expenseRequestDTO(
                payer.getId(),
                Map.of(
                        payer.getId(), payerShare,
                        participant.getId(), participantShare
                ),
                category.getId());


        // === Mocking ===
        doNothing().when(travelMemberService).validateAllUsersInTravel(eq(travelId), anySet());
        doReturn(travel).when(travelService).findTravel(travelId);
        doReturn(category).when(categoryService).findCategory(category.getId());
        doReturn(payer).when(travelMemberService).findUserInTravel(payer.getId(), travelId);
        doReturn(participant).when(travelMemberService).findUserInTravel(participant.getId(), travelId);

        doAnswer(invocation -> {
            Travel t = invocation.getArgument(0);
            Expense e = invocation.getArgument(1);
            e.setTravel(t);
            return null;
        }).when(travelService).addExpense(any(Travel.class), any(Expense.class));

        doAnswer(invocation -> {
            Expense exp = invocation.getArgument(0);
            MemberExpense memberExp = invocation.getArgument(1);
            exp.getMemberExpenses().add(memberExp);
            return null;
        }).when(memberExpenseService).addMemberExpense(any(Expense.class), any(MemberExpense.class));


        // === When ===
        ExpenseResponseDTO actual = expenseService.createExpense(requestDTO, travelId);


        // === Then ===
        assertAll(
                () -> assertThat(actual).isNotNull(),

                // Проверка полей ответа
                () -> assertThat(actual.name()).isEqualTo(requestDTO.getName()),
                () -> assertThat(actual.description()).isEqualTo(requestDTO.getDescription()),
                () -> assertThat(actual.payerId()).isEqualTo(requestDTO.getPayerId()),
                () -> assertThat(actual.sum()).isEqualByComparingTo(payerShare.add(participantShare)),
                () -> assertThat(actual.categoryId()).isEqualTo(requestDTO.getCategoryId()),
                () -> assertThat(actual.categoryName()).isEqualTo(category.getName()),

                // Проверка участников
                () -> {
                    assertThat(actual.participants())
                            .hasSize(2)
                            .extracting(MemberExpenseResponseDTO::userId)
                            .containsExactlyInAnyOrder(payer.getId(), participant.getId());
                },

                // Проверка долей участников
                () -> {
                    for (MemberExpenseResponseDTO p : actual.participants()) {
                        if (p.userId().equals(payer.getId())) {
                            assertThat(p.share()).isEqualByComparingTo(payerShare);
                        } else if (p.userId().equals(participant.getId())) {
                            assertThat(p.share()).isEqualByComparingTo(participantShare);
                        } else {
                            fail("Unexpected participant id: " + p.userId());
                        }
                    }
                },

                // --- проверка реальных MemberExpense, что в БД положительные/отрицательные доли ---
                () -> {
                    ArgumentCaptor<MemberExpense> captor = ArgumentCaptor.forClass(MemberExpense.class);
                    verify(memberExpenseService, times(2))
                            .addMemberExpense(any(Expense.class), captor.capture());

                    List<MemberExpense> saved = captor.getAllValues();
                    for (MemberExpense me : saved) {
                        if (me.getParticipant().getId().equals(payer.getId())) {
                            assertThat(me.getShare())
                                    .as("Плательщик должен иметь положительную долю")
                                    .isEqualByComparingTo(payerShare);
                        } else if (me.getParticipant().getId().equals(participant.getId())) {
                            assertThat(me.getShare())
                                    .as("Участник должен иметь отрицательную долю")
                                    .isEqualByComparingTo(participantShare.negate());
                        } else {
                            fail("Unexpected participant id: " + me.getParticipant().getId());
                        }
                    }
                }
        );


        // === VERIFY ===
        verify(travelService).findTravel(travelId);
        verify(categoryService).findCategory(category.getId());
        verify(travelMemberService, atLeastOnce()).findUserInTravel(payer.getId(), travelId);
        verify(travelMemberService).findUserInTravel(participant.getId(), travelId);
        verify(travelMemberService).validateAllUsersInTravel(eq(travelId), anySet());
        verify(travelService).addExpense(eq(travel), any(Expense.class));
        verify(memberExpenseService, times(2))
                .addMemberExpense(any(Expense.class), any(MemberExpense.class));
    }


    @DisplayName("Ошибка: список участников траты пуст (создание траты)")
    @Test
    void throwInvalidParticipantShareExceptionIfParticipantSharesIsEmpty() {

        // === Given ===
        ExpenseRequestDTO requestDTO = TestDataFactory.expenseRequestDTO(
                1L,
                Map.of(),
                1L);


        // === When & Then ===
        assertThrows(
                InvalidParticipantShareException.class,
                () -> expenseService.createExpense(requestDTO, 1L)
        );


        // === VERIFY ===
        verifyNoInteractions(
                travelMemberService,
                categoryService,
                travelService,
                memberExpenseService
        );
    }

// TODO в сервсие получать список всех сразу, там 2 раза получаю платящего

//    @Test
//    void throwUserNotFoundInTravelException() {
//
//        Long travelId = 1L;
//
//        ExpenseRequestDTO requestDTO = ExpenseTestFactory.createExpenseRequestDTO(
//                "Ужин в ресторане",
//                1L,
//                Map.of(1L, BigDecimal.valueOf(100)),
//                1L);
//
//        doThrow(new UserNotFoundInTravelException("User not found"))
//                .when(travelMemberService)
//                .validateAllUsersInTravel(eq(travelId), anySet());
//
//        assertThrows(UserNotFoundInTravelException.class,
//                () -> expenseService.createExpense(requestDTO, travelId));
//    }


    @DisplayName("Ошибка: плательщик отсутствует среди участников траты (создание траты)")
    @Test
    void throwPayerNotInParticipantsException() {

        // === Given ===
        Long travelId = 1L;
        ExpenseRequestDTO requestDTO = TestDataFactory.expenseRequestDTO(
                100L,
                Map.of(
                        1L, BigDecimal.valueOf(100),
                        2L, BigDecimal.valueOf(50)
                ),
                1L);


        // === Mocking ===
        doNothing().when(travelMemberService).validateAllUsersInTravel(eq(travelId), anySet());


        // === When & Then ===
        assertThrows(PayerNotInParticipantsException.class, () -> expenseService.createExpense(requestDTO, travelId));


        // === VERIFY ===
        verify(travelMemberService).validateAllUsersInTravel(eq(travelId), anySet());
        verifyNoMoreInteractions(travelMemberService);
        verifyNoInteractions(
                categoryService,
                travelService,
                memberExpenseService
        );

    }


    @DisplayName("Ошибка: доля участника траты некорректна (создание траты)")
    @ParameterizedTest
    @NullSource
    @ValueSource(doubles = {-99.0})
    void throwInvalidParticipantShareExceptionForNullOrNegativeShare(Double invalidShare) {

        // === Given ===
        Long travelId = 1L;
        Map<Long, BigDecimal> participantShares = new HashMap<>();
        participantShares.put(1L, invalidShare == null ? null : BigDecimal.valueOf(invalidShare));

        ExpenseRequestDTO requestDTO = TestDataFactory.expenseRequestDTO(1L, participantShares,1L); //TODO категория не нужна


        // === When & Then ===
        assertThrows(InvalidParticipantShareException.class, () -> expenseService.createExpense(requestDTO, travelId));


        // === VERIFY ===
        verify(travelMemberService).validateAllUsersInTravel(eq(travelId), anySet());
        verifyNoMoreInteractions(travelMemberService);
        verifyNoInteractions(
                categoryService,
                travelService,
                memberExpenseService
        );
    }


    @DisplayName("Ошибка: категория траты не найдена (создание траты)")
    @Test
    void throwCategoryNotFoundException() {

        // === Given ===
        Long travelId = 1L;
        ExpenseRequestDTO requestDTO = TestDataFactory.expenseRequestDTO(
                1L,
                Map.of(1L, BigDecimal.valueOf(100)));


        // === Mocking ===
        doThrow(new CategoryNotFoundException("Category not found"))
                .when(categoryService)
                .findCategory(null);


        // === When & Then ===
        assertThrows(CategoryNotFoundException.class, () -> expenseService.createExpense(requestDTO, travelId));


        // === VERIFY ===
        verify(travelMemberService).validateAllUsersInTravel(eq(travelId), anySet());
        verifyNoMoreInteractions(
                travelMemberService,
                categoryService);
        verifyNoInteractions(
                travelService,
                memberExpenseService
        );
    }


    @DisplayName("Ошибка: путешествие не найдено (создание траты)")
    @Test
    void throwTravelNotFoundException() {

        // === Given ===
        Long travelId = 1L,
                payerId = 1L;


        ExpenseRequestDTO requestDTO = TestDataFactory.expenseRequestDTO(
                payerId,
                Map.of(payerId, BigDecimal.valueOf(100)));


        // === Mocking ===
        doThrow(new TravelNotFoundException("Travel not found"))
                .when(travelService)
                .findTravel(eq(travelId));


        // === When & Then ===
        assertThrows(TravelNotFoundException.class, () -> expenseService.createExpense(requestDTO, travelId));


        // === VERIFY ===
        verify(travelMemberService).validateAllUsersInTravel(eq(travelId), anySet());
        verifyNoMoreInteractions(travelMemberService);
        verifyNoInteractions(memberExpenseService);
    }


    @DisplayName("Ошибка: попытка создать дубликат траты")
    @Test
    void throwDuplicateExpenseExceptionIf() {

        // === Given ===
        Long travelId = 1L;
        Travel travel = TestDataFactory.travel(travelId);
        Category category = TestDataFactory.category(3L);
        User payer = TestDataFactory.user(1L);

        ExpenseRequestDTO requestDTO = TestDataFactory.expenseRequestDTO(
                payer.getId(),
                Map.of(payer.getId(), BigDecimal.valueOf(100)),
                category.getId());


        // === Mocking ===
        doNothing().when(travelMemberService).validateAllUsersInTravel(eq(travelId), anySet());
        doReturn(travel).when(travelService).findTravel(travelId);
        doReturn(category).when(categoryService).findCategory(category.getId());
        doReturn(payer).when(travelMemberService).findUserInTravel(payer.getId(), travelId);
        doThrow(new DuplicateExpenseException("Expense duplicate"))
                .when(travelService)
                .addExpense(any(Travel.class), any(Expense.class));


        // === When & Then ===
        assertThrows(DuplicateExpenseException.class, () -> expenseService.createExpense(requestDTO, travelId));


        // === VERIFY ===
        verify(travelMemberService).validateAllUsersInTravel(eq(travelId), anySet());
        verifyNoMoreInteractions(travelMemberService);
        verifyNoInteractions(memberExpenseService);
    }
}
