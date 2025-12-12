package com.tbank.ttravels_backend.factory;

import com.tbank.ttravels_backend.entity.Category;
import com.tbank.ttravels_backend.entity.Expense;
import com.tbank.ttravels_backend.entity.User;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;


//@Component
public class ExpenseFactory {


    public static Expense create(String name, String description, BigDecimal sum, OffsetDateTime date,
                                 Category category, User payer) {

        return Expense.builder()
                .name(name)
                .description(description)
                .sum(sum)
                .date(date)
                .category(category)
                .payer(payer)
                .memberExpenses(new HashSet<>())
                .build();
    }

}
