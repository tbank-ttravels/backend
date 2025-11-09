package com.tbank.ttravels_backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "sum")
    private BigDecimal sum;

    @Column(name = "date")
    private OffsetDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id")
    private User payer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_id")
    private Travel travel;

    @OneToMany(cascade = CascadeType.ALL,
            mappedBy = "expense",
            orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private Set<MemberExpense> memberExpenses = new HashSet<>();

    // TODO
//    @ManyToOne
//    @JoinColumn(name= "category_id")
//    private CategoryExpenses categoryExpenses;


    // Фабричный метод создания Траты
    public static Expense create(String name, String description, BigDecimal sum,
                                 OffsetDateTime date, User payer, Travel travel) {

        if (payer == null)
            throw new IllegalArgumentException("Expense creation failed: payer must not be null");

        if (travel == null)
            throw new IllegalArgumentException("Expense creation failed: travel must not be null");

        if (sum == null)
            throw new IllegalArgumentException("Expense creation failed: sum must not be null");

        if (sum.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Expense creation failed: sum must be greater than zero");

        Expense expense = new Expense();

        expense.name = name;
        expense.description = description;
        expense.sum = sum;
        expense.date = date;
        expense.payer = payer;
        expense.travel = travel;

        return expense;
    }

    // Метод для добавления участника траты
    public void addMemberExpense(MemberExpense memberExpense) {

        if (memberExpense != null) {
            memberExpenses.add(memberExpense);
            memberExpense.setExpense(this);
        }
    }

    // Метод для удаления участника траты
    public void removeMemberExpense(MemberExpense memberExpense) {

        if (memberExpense != null) {
            this.memberExpenses.remove(memberExpense);
            memberExpense.setExpense(null);
        }
    }
}