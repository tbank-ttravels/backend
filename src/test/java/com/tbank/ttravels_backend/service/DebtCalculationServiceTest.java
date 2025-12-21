package com.tbank.ttravels_backend.service;


import com.tbank.ttravels_backend.dto.debt.DebtInfoDTO;
import com.tbank.ttravels_backend.dto.debt.TravelDebtsResponseDTO;
import com.tbank.ttravels_backend.entity.*;
import com.tbank.ttravels_backend.mapper.UserDtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DebtCalculationServiceTest {

    @Mock
    private TravelMemberService travelMemberService;
    @Mock
    private TransferService transferService;
    @Mock
    private ExpenseService expenseService;
    private final UserDtoMapper userDtoMapper = new UserDtoMapper();
    private DebtCalculationService debtCalculationService;


    @BeforeEach
    void init() {

        debtCalculationService = new DebtCalculationService(
                travelMemberService,
                transferService,
                expenseService,
                userDtoMapper
        );
    }


    @DisplayName("Расчет долгов пользователя с учетом расходов и переводов")
    @Test
    void calculateDebtsForUser() {

        // === Given ===
        Long travelId = 1L;

        User currentUser = TestDataFactory.user(1);
        User debtor = TestDataFactory.user(2);
        User creditor = TestDataFactory.user(3);

        List<TravelMember> travelMembers = TestDataFactory.listTravelMember(currentUser, debtor, creditor);

        // Expense 1
        MemberExpense meCurrentE1 = TestDataFactory.memberExpense(currentUser, 1000);
        MemberExpense meDebtorE1 = TestDataFactory.memberExpense(debtor, -1500);
        MemberExpense meCreditorE1 = TestDataFactory.memberExpense(creditor, -400);

        // Expense 2
        MemberExpense meCurrentE2 = TestDataFactory.memberExpense(currentUser, 700);
        MemberExpense meDebtorE2 = TestDataFactory.memberExpense(debtor, -1350);
        MemberExpense meCreditorE2 = TestDataFactory.memberExpense(creditor, -450);

        // Debtor Expense
        MemberExpense meCurrentDE = TestDataFactory.memberExpense(currentUser, -700);
        MemberExpense meDebtorDE = TestDataFactory.memberExpense(debtor, -380);
        MemberExpense meCreditorDE = TestDataFactory.memberExpense(creditor, 1450);

        Expense expense1 = TestDataFactory.expense(currentUser, meCurrentE1, meDebtorE1, meCreditorE1);
        Expense expense2 = TestDataFactory.expense(currentUser, meCurrentE2, meDebtorE2, meCreditorE2);
        Expense debtorExpense = TestDataFactory.expense(creditor, meCurrentDE, meDebtorDE, meCreditorDE);

        List<Expense> expenses = List.of(expense1, expense2);
        List<Expense> debtorExpenses = List.of(debtorExpense);

        Transfer transfer1 = TestDataFactory.transfer(currentUser, creditor, 320);
        Transfer transfer2 = TestDataFactory.transfer(currentUser, debtor, 280);
        Transfer transfer3 = TestDataFactory.transfer(creditor, currentUser, 4000);

        List<Transfer> incoming = List.of(transfer3);
        List<Transfer> outgoing = List.of(transfer1, transfer2);


        // === Mocking ===
        doReturn(travelMembers).when(travelMemberService)
                .findAllMembersInTravel(travelId);
        doReturn(expenses).when(expenseService)
                .findAllByTravelIdAndPayerIdOrderByDateDesc(travelId, currentUser.getId());
        doReturn(debtorExpenses).when(expenseService)
                .findExpensesWhereUserIsDebtor(travelId, currentUser.getId());
        doReturn(incoming).when(transferService)
                .findAllByTravelIdAndRecipientId(travelId, currentUser.getId());
        doReturn(outgoing).when(transferService)
                .findAllByTravelIdAndSenderId(travelId, currentUser.getId());


        // === When ===
        TravelDebtsResponseDTO actual = debtCalculationService.calculateDebtsForUser(currentUser.getId(), travelId);


        // === Then ===
        // debtor должен current
        BigDecimal debtorOwesCurrent = BigDecimal.ZERO
                .add(meDebtorE1.getShare())
                .add(meDebtorE2.getShare())
                .add(transfer2.getSum().negate())
                .abs();

        // current должен creditor
        BigDecimal currentOwesCreditor = BigDecimal.ZERO
                .add(meCreditorE1.getShare())
                .add(meCreditorE2.getShare())
                .add(meCurrentDE.getShare().abs())
                .add(transfer1.getSum().negate())
                .add(transfer3.getSum())
                .abs();

        assertAll(
                () -> assertThat(actual.debts()).hasSize(1),
                () -> assertDebtInfoMatches(creditor, currentOwesCreditor,
                        actual.debts().get(0), "Creditor debt"),

                () -> assertThat(actual.creditors()).hasSize(1),
                () -> assertDebtInfoMatches(debtor, debtorOwesCurrent,
                        actual.creditors().get(0), "Debtor credit")
        );


        // === VERIFY ===
        verify(travelMemberService).findAllMembersInTravel(travelId);
        verify(expenseService).findAllByTravelIdAndPayerIdOrderByDateDesc(travelId, currentUser.getId());
        verify(expenseService).findExpensesWhereUserIsDebtor(travelId, currentUser.getId());
        verify(transferService).findAllByTravelIdAndRecipientId(travelId, currentUser.getId());
        verify(transferService).findAllByTravelIdAndSenderId(travelId, currentUser.getId());

        verifyNoMoreInteractions(travelMemberService, expenseService, transferService);
    }

    private void assertDebtInfoMatches(User expectedUser, BigDecimal expectedAmount,
                                       DebtInfoDTO actualDebtInfo, String context) {
        assertAll(
                () -> assertThat(actualDebtInfo.user().id())
                        .as(context + " - User ID mismatch")
                        .isEqualTo(expectedUser.getId()),

                () -> assertThat(actualDebtInfo.user().name())
                        .as(context + " - User name mismatch")
                        .isEqualTo(expectedUser.getName()),

                () -> assertThat(actualDebtInfo.user().surname())
                        .as(context + " - User surname mismatch")
                        .isEqualTo(expectedUser.getSurname()),

                () -> assertThat(actualDebtInfo.totalAmount())
                        .as(context + " - Amount mismatch")
                        .isEqualByComparingTo(expectedAmount)
        );
    }
}
