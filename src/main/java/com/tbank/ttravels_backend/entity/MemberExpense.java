package com.tbank.ttravels_backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Entity
@Table(name = "members_expenses")
@Getter
@Setter
@NoArgsConstructor
public class MemberExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id")
    private Expense expense;

    @Column(name = "share")
    private BigDecimal share;


    // Фабричный метод создания участника траты.
    // К трате привязывается в классе Expense с помощью addMemberExpense
    public static MemberExpense create(User participant, BigDecimal share) {

        if (participant == null)
            throw new IllegalArgumentException("MemberExpense creation failed: participant must not be null");

        if (share == null)
            throw new IllegalArgumentException("MemberExpense creation failed: share must not be null");

        if (share.compareTo(BigDecimal.ZERO) == 0)
            throw new IllegalArgumentException("MemberExpense creation failed: share must not be zero");


        MemberExpense memberExpense = new MemberExpense();

        memberExpense.participant = participant;
        memberExpense.share = share;

        return memberExpense;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MemberExpense that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}