package com.tbank.ttravels_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Entity
@Table(name = "members_expenses")
@Getter
@NoArgsConstructor
public class MemberExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

        MemberExpense memberExpense = new MemberExpense();

        memberExpense.setParticipant(participant);
        memberExpense.setShare(share);

        return memberExpense;
    }

    public void setParticipant(User participant) {

        if (participant == null)
            throw new IllegalArgumentException("Не удалось создать участника расхода: участник не может быть null");

        this.participant = participant;
    }

    public void setShare(BigDecimal share) {

        if (share == null)
            throw new IllegalArgumentException("Не удалось создать участника расхода: доля не может быть null");

        if (share.compareTo(BigDecimal.ZERO) == 0)
            throw new IllegalArgumentException("Не удалось создать участника расхода: доля не может быть нулевой");

        this.share = share;
    }

    void setExpense(Expense expense) {
        this.expense = expense;
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