package com.tbank.ttravels_backend.dto.travel.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "Ответ с приглашениями в поездки")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvitesResponse {
    @Schema(description = "Список приглашений в поездки")
    private List<InvitesItem> invites;
}