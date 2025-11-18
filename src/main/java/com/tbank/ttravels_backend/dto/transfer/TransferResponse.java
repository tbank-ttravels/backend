package com.tbank.ttravels_backend.dto.transfer;

import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
public class TransferResponse {
    private Long id;
    private Long travelId;
    private Long senderId;
    private Long recipientId;
    private BigDecimal sum;
    private OffsetDateTime date;
}
