package com.tbank.ttravels_backend.dto.travel;

import com.tbank.ttravels_backend.dto.travel.validator.ValidDateRange;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ValidDateRange
public class CreateTravelRequest {
    @NotBlank
    private String name;
    private String description;
    @NotNull
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
}
