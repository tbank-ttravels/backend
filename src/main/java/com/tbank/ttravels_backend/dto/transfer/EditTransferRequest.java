package com.tbank.ttravels_backend.dto.transfer;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter
public class EditTransferRequest {
    private BigDecimal sum;
}
