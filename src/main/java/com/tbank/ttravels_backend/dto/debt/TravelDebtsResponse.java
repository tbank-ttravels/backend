package com.tbank.ttravels_backend.dto.debt;

import lombok.*;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TravelDebtsResponse {

    private List<DebtInfo> debts;        // кому пользователь должен
    private List<DebtInfo> creditors;    // кто должен пользователю
}
