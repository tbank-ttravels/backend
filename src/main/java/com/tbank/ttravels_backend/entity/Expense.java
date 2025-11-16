package com.tbank.ttravels_backend.entity;

import jakarta.persistence.*;
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
@NoArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "name")
    private String name;

    @Setter
    @Column(name = "description")
    private String description;

    @Column(name = "sum")
    private BigDecimal sum;

    @Setter
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
    private Set<MemberExpense> memberExpenses = new HashSet<>();

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;


    // Фабричный метод создания Траты
    public static Expense create(String name, String description, BigDecimal sum, OffsetDateTime date,
                                 Category category, User payer, Travel travel) {

        Expense expense = new Expense();

        expense.setSum(sum);
        expense.setPayer(payer);
        expense.setTravel(travel);
        expense.name = name;
        expense.description = description;
        expense.date = date;
        expense.category = category;

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

    public void setSum(BigDecimal sum) {

        if (sum == null)
            throw new IllegalArgumentException("Expense creation failed: sum must not be null");

        if (sum.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Expense creation failed: sum must be greater than zero");

        this.sum = sum;
    }

    public void setPayer(User payer) {

        if (payer == null)
            throw new IllegalArgumentException("Expense creation failed: payer must not be null");

        this.payer = payer;
    }

    public void setTravel(Travel travel) {

        if (travel == null)
            throw new IllegalArgumentException("Expense creation failed: travel must not be null");

        this.travel = travel;
    }
}