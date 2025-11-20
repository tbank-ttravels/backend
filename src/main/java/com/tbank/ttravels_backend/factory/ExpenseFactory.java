package com.tbank.ttravels_backend.factory;

import com.tbank.ttravels_backend.entity.*;
import com.tbank.ttravels_backend.service.MemberExpenseService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;


// TODO стоит ли тут делать проверки
@Component
public class ExpenseFactory {

    private final MemberExpenseService memberExpenseService;


    public ExpenseFactory(MemberExpenseService memberExpenseService) {
        this.memberExpenseService = memberExpenseService;
    }


    public Expense create(String name, String description, BigDecimal sum, OffsetDateTime date,
                          Category category, User payer, Travel travel) {

        return Expense.builder()
                .name(name)
                .description(description)
                .sum(sum)
                .date(date)
                .category(category)
                .payer(payer)
                .travel(travel) // TODO через travel
                .build();
    }


    public Expense create(String name, String description, BigDecimal sum, OffsetDateTime date,
                          Set<MemberExpense> memberExpenses, Category category, User payer, Travel travel) {

        Expense expense = this.create(name, description, sum, date, category, payer, travel);

        memberExpenses.forEach(me -> memberExpenseService.addMemberExpense(expense, me));

        return expense;
    }


    public Expense create(Long id, String name, String description, BigDecimal sum, OffsetDateTime date,
                          Set<MemberExpense> memberExpenses, Category category, User payer, Travel travel) {

        Expense expense = this.create(name, description, sum, date, memberExpenses, category, payer, travel);

        expense.setId(id);

        return expense;
    }
}
