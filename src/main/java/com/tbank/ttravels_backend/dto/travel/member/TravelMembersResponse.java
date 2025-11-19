package com.tbank.ttravels_backend.dto.travel.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TravelMembersResponse {
    List<TravelMemberItem> members;
}
