package com.tbank.ttravels_backend.dto.transfer;

import lombok.*;
import java.util.List;

@Getter
@AllArgsConstructor
public class TransfersListResponse {
    private List<TransferResponse> transfers;
}
