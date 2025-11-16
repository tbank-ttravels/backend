package com.tbank.ttravels_backend.dto.transfer;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter
public class CreateTransferRequest {
    private Long travelId;
    private Long senderId;
    private Long recipientId;
    private BigDecimal sum;
}
