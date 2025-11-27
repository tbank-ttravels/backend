package com.tbank.ttravels_backend.service;


import com.tbank.ttravels_backend.entity.Expense;
import com.tbank.ttravels_backend.entity.MemberExpense;
import com.tbank.ttravels_backend.exception.DuplicateParticipantException;
import org.springframework.stereotype.Service;

@Service
public class MemberExpenseService {

    // Метод для добавления участника траты
    public void addMemberExpense(Expense expense,
                                 MemberExpense memberExpense) {

        checkExpenseAndMemberExpenseIsNull(expense, memberExpense);

        if (expense.getMemberExpenses().contains(memberExpense)) {
            throw new DuplicateParticipantException("Участник с id = " + memberExpense.getId() +
                    "уже участвует в трате '" + expense.getName() + "'");
        }

        expense.getMemberExpenses().add(memberExpense);
        memberExpense.setExpense(expense);
    }

    // Метод для удаления участника траты
    public void removeMemberExpense(Expense expense,
                                    MemberExpense memberExpense) {

        checkExpenseAndMemberExpenseIsNull(expense, memberExpense);

        expense.getMemberExpenses().remove(memberExpense);
        memberExpense.setExpense(null);
    }

    private void checkExpenseAndMemberExpenseIsNull(Expense expense,
                                                    MemberExpense memberExpense) {

        if (expense == null) {
            throw new IllegalArgumentException("Expense is null");
        }
        if (memberExpense == null) {
            throw new IllegalArgumentException("MemberExpense is null");
        }
    }
}
