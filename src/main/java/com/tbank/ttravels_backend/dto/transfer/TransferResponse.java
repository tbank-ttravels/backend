package com.tbank.ttravels_backend.dto.transfer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponse {
    private Long id;
    private Long travelId;
    private Long senderId;
    private Long recipientId;
    private BigDecimal sum;
    private OffsetDateTime date;
}
