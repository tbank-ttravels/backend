package com.tbank.ttravels_backend.dto.transfer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransfersListResponse {
    private List<TransferResponse> transfers;
}
