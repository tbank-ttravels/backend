package com.tbank.ttravels_backend.dto.debt;

import lombok.*;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DebtInfo {

    private UserDTO user;                 // другой участник (кому или от кого)
    private BigDecimal totalAmount;       // сумма долга
}
