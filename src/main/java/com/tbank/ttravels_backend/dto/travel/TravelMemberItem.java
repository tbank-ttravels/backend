package com.tbank.ttravels_backend.dto.travel;

import com.tbank.ttravels_backend.enums.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TravelMemberItem {
    private Long id;
    private String name;
    private String phone;
    private MemberStatus status;
}
