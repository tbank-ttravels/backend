package com.tbank.ttravels_backend.dto.travel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditTravelRequest {
    private String name;
    private String description;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
}
