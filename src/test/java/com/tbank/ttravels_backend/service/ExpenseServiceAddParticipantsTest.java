package com.tbank.ttravels_backend.service;

import com.tbank.ttravels_backend.dto.exspense.ExpenseResponseDTO;
import com.tbank.ttravels_backend.dto.exspense.MemberExpenseResponseDTO;
import com.tbank.ttravels_backend.entity.Category;
import com.tbank.ttravels_backend.entity.Expense;
import com.tbank.ttravels_backend.entity.MemberExpense;
import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.exception.*;
import com.tbank.ttravels_backend.mapper.ExpenseDtoMapper;
import com.tbank.ttravels_backend.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ExpenseServiceAddParticipantsTest {

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


    @DisplayName("Успешное добавление новых участников в трату")
    @Test
    void addParticipantsToExpense() {

        // === Given ===
        Long travelId = 1L,
                expenseId = 2L,
                me1Id = 3L,
                me2Id = 4L,
                newParticipantId = 30L;

        BigDecimal shareNewParticipant = BigDecimal.valueOf(500L),
                valuePayer = BigDecimal.valueOf(100),
                valueMe2 = BigDecimal.valueOf(-150);

        BigDecimal expectedSum = valuePayer.add(valueMe2.abs()).add(shareNewParticipant);

        Map<Long, BigDecimal> participantShare = Map.of(newParticipantId, shareNewParticipant);

        User payer = User.builder().id(me1Id).build(),
                participant = User.builder().id(me2Id).build(),
                newParticipant = User.builder().id(newParticipantId).build();

        MemberExpense me1 = MemberExpense.builder().participant(payer).share(valuePayer).build(),
                me2 = MemberExpense.builder().participant(participant).share(valueMe2).build();

        Expense expense = Expense.builder()
                .id(expenseId)
                .payer(payer)
                .category(Category.builder().id(2L).build())
                .memberExpenses(new HashSet<>(Set.of(me1, me2)))
                .sum(valuePayer.add(valueMe2.abs()))
                .build();


        // === Mocking ===
        doNothing().when(travelMemberService).validateAllUsersInTravel(travelId, participantShare.keySet());
        doReturn(Optional.of(expense)).when(expenseRepository).findByIdAndTravelId(expenseId, travelId);
        doReturn(List.of(newParticipant)).when(userService).getUsers(participantShare.keySet());

        doAnswer(inv -> {
            Expense exp = inv.getArgument(0);
            MemberExpense newME = inv.getArgument(1);
            exp.getMemberExpenses().add(newME);
            return null;
        }).when(memberExpenseService).addMemberExpense(any(), any());


        // === When ===
        ExpenseResponseDTO actual = expenseService.addParticipantsToExpense(travelId, expenseId, participantShare);


        // === Then ===
        assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> assertThat(actual.sum()).isEqualByComparingTo(expectedSum),

                () -> assertThat(actual.participants()).hasSize(3),

                // DTO содержит нового участника
                () -> assertThat(actual.participants())
                        .extracting(MemberExpenseResponseDTO::userId)
                        .contains(newParticipantId),

                // проверка, что в DTO доля нового участника положительная
                () -> {
                    actual.participants().stream()
                            .filter(p -> p.userId().equals(newParticipantId))
                            .findFirst()
                            .ifPresent(p -> assertThat(p.share())
                                    .as("DTO share should be positive")
                                    .isEqualByComparingTo(shareNewParticipant));
                },

                // TODO Это норм?
                // проверка доли новой сущности
                () -> expense.getMemberExpenses().forEach(me -> {
                    if (me.getParticipant().getId().equals(newParticipantId)) {
                        assertThat(me.getShare()).isEqualByComparingTo(shareNewParticipant.negate());
                    }
                })
        );
    }


    @DisplayName("Ошибка: список новых участников траты пуст")
    @Test
    void throwEmptyParticipantsListException() {

        // === When & Then ===
        assertThrows(EmptyParticipantsListException.class,
                () -> expenseService.addParticipantsToExpense(1L, 1L, Map.of()));

        // === VERIFY ===
        verifyNoInteractions(expenseRepository, travelMemberService, userService, memberExpenseService);
    }


    @DisplayName("Ошибка: доля участника траты некорректна (≤ 0)")
    @ParameterizedTest
    @NullSource
    @ValueSource(doubles = {-99.0})
    void throwInvalidParticipantShareException(Double invalidShare) {

        // === GIVEN ===
        Map<Long, BigDecimal> participantShares = new HashMap<>();
        participantShares.put(1L, invalidShare == null ? null : BigDecimal.valueOf(invalidShare));


        // === When & Then ===
        assertThrows(
                InvalidParticipantShareException.class,
                () -> expenseService.addParticipantsToExpense(1L, 2L, participantShares)
        );

        // === VERIFY ===
        verifyNoInteractions(expenseRepository, travelMemberService, userService, memberExpenseService);
    }


    @DisplayName("Ошибка: трата не найдена в указанном путешествии")
    @Test
    void throwExpenseNotFoundInTravelException() {

        // === Given ===
        Long travelId = 1L;
        Long expenseId = 2L;


        // === Mocking ===
        doReturn(Optional.empty()).when(expenseRepository).findByIdAndTravelId(expenseId, travelId);

        // === When & Then ===
        assertThrows(ExpenseNotFoundInTravelException.class,
                () -> expenseService.addParticipantsToExpense(travelId, expenseId, Map.of(1L, BigDecimal.TEN)));

        // === VERIFY ===
        verifyNoInteractions(travelMemberService, userService, memberExpenseService);
    }


    @DisplayName("Ошибка: новый участник траты не принадлежит путешествию")
    @Test
    void throwUserNotFoundInTravelException() {

        // === Given ===
        Long travelId = 1L;
        Long expenseId = 2L;
        Map<Long, BigDecimal> participantShare = Map.of(1L, BigDecimal.valueOf(100));
        Expense expense = TestDataFactory.expense(expenseId);


        // === Mocking ===
        doReturn(Optional.of(expense)).when(expenseRepository).findByIdAndTravelId(expenseId, travelId);
        doThrow(UserNotFoundInTravelException.class)
                .when(travelMemberService).validateAllUsersInTravel(travelId, participantShare.keySet());


        // === When & Then ===
        assertThrows(UserNotFoundInTravelException.class,
                () -> expenseService.addParticipantsToExpense(travelId, expenseId, participantShare));


        // === VERIFY ===
        verifyNoInteractions(userService, memberExpenseService);
    }


    @DisplayName("Ошибка: участник траты уже есть в трате(дубликат)")
    @Test
    void throwDuplicateParticipantException() {

        // === Given ===
        Long travelId = 1L;
        Long expenseId = 2L;
        Long duplicateParticipantId = 1L;
        Map<Long, BigDecimal> newParticipantShare = Map.of(duplicateParticipantId, BigDecimal.valueOf(100));
        Expense expense = TestDataFactory.expense(expenseId, duplicateParticipantId, 2L);


        // === Mocking ===
        doNothing().when(travelMemberService).validateAllUsersInTravel(travelId, newParticipantShare.keySet());
        doReturn(Optional.of(expense)).when(expenseRepository).findByIdAndTravelId(expenseId, travelId);


        // === When & Then ===
        assertThrows(DuplicateParticipantException.class,
                () -> expenseService.addParticipantsToExpense(travelId, expenseId, newParticipantShare));


        // === VERIFY ===
        verifyNoInteractions(userService, memberExpenseService);
    }
}
