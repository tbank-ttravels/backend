package com.tbank.ttravels_backend.factory;

import com.tbank.ttravels_backend.entity.MemberExpense;
import com.tbank.ttravels_backend.entity.User;

import java.math.BigDecimal;

public class MemberExpenseFactory {

    public static MemberExpense create(User participant, BigDecimal share) {

        return MemberExpense.builder()
                .participant(participant)
                .share(share)
                .build();
    }
}
