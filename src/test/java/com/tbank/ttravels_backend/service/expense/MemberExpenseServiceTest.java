package com.tbank.ttravels_backend.service.expense;

import com.tbank.ttravels_backend.entity.Expense;
import com.tbank.ttravels_backend.entity.MemberExpense;
import com.tbank.ttravels_backend.exception.DuplicateParticipantException;
import com.tbank.ttravels_backend.service.MemberExpenseService;
import com.tbank.ttravels_backend.service.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MemberExpenseServiceTest {

    private final MemberExpenseService memberExpenseService = new MemberExpenseService();


    @Test
    void addMemberExpense() {

        // === Given ===
        Expense expense = TestDataFactory.expense(1L);
        List<MemberExpense> memberExpenseList =
                List.of(TestDataFactory.memberExpense(2L, 100),
                        TestDataFactory.memberExpense(3L, -200),
                        TestDataFactory.memberExpense(4L, -300));


        // === When ===
        for (var me : memberExpenseList) {
            memberExpenseService.addMemberExpense(expense, me);
        }

        // === Then ===
        assertAll(
                () -> assertThat(expense.getMemberExpenses()).isNotEmpty(),
                () -> assertExpenseContainsAllMembers(expense, memberExpenseList),
                () -> assertMembersContainExpense(memberExpenseList, expense)
        );
    }

    private void assertExpenseContainsAllMembers(Expense expense, List<MemberExpense> expectedMembers) {

        // === When & Then ===
        assertThat(expense.getMemberExpenses())
                .as("Expense must contain all provided member expenses")
                .isNotNull()
                .containsExactlyInAnyOrderElementsOf(expectedMembers);
    }

    private void assertMembersContainExpense(List<MemberExpense> memberExpenses, Expense expectedExpense) {

        // === When & Then ===
        assertThat(memberExpenses)
                .as("Every MemberExpense must reference the Expense")
                .isNotNull()
                .allSatisfy(me -> assertThat(me.getExpense().getId())
                        .isEqualTo(expectedExpense.getId()));
    }


    @Test
    void removeMemberExpense() {

        // === Given ===
        Expense expense = TestDataFactory.expense(1L);

        MemberExpense me1 = TestDataFactory.memberExpense(2L, 100, expense);
        MemberExpense me2Deleted = TestDataFactory.memberExpense(3L, -200, expense);
        MemberExpense me3 = TestDataFactory.memberExpense(4L, -300, expense);
        expense.setMemberExpenses(new HashSet<>(Set.of(me1, me2Deleted, me3)));


        // === When ===
        memberExpenseService.removeMemberExpense(expense, me2Deleted);


        // === Then ===
        assertAll(
                () -> assertThat(expense.getMemberExpenses()).isNotEmpty(),
                () -> assertExpenseContainsAllMembers(expense, List.of(me1, me3)),
                () -> assertThat(me2Deleted.getExpense())
                        .as("Deleted MemberExpense must have null Expense")
                        .isNull()
        );
    }

    @Test
    void throwDuplicateParticipantException() {

        // === Given ===
        Expense expense = TestDataFactory.expense(1L);

        MemberExpense me1 = TestDataFactory.memberExpense(2L, 100, expense);
        MemberExpense me2 = TestDataFactory.memberExpense(3L, -200, expense);
        expense.setMemberExpenses(new HashSet<>(Set.of(me1, me2)));


        // === When & Then ===
        assertThrows(DuplicateParticipantException.class,
                () -> memberExpenseService.addMemberExpense(expense, me2));
    }
}