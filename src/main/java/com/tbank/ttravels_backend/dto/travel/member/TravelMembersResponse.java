package com.tbank.ttravels_backend.dto.travel.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "Ответ с информацией об участниках поездки")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TravelMembersResponse {
    @Schema(description = "Список участников поездки")
    private List<TravelMemberItem> members;
}
