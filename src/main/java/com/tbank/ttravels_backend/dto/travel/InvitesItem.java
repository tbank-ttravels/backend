package com.tbank.ttravels_backend.dto.travel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvitesItem {
    private Long inviteId;
    private String travelName;
    private String description;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
}
