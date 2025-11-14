package com.tbank.ttravels_backend.dto.travel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tbank.ttravels_backend.enums.TravelStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TravelResponse {
    private Long id;
    private String name;
    private String description;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private TravelStatus status;
}
