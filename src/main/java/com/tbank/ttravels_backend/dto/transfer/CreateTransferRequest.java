package com.tbank.ttravels_backend.dto.transfer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTransferRequest {
    private Long senderId;
    private Long recipientId;
    private BigDecimal sum;
}
