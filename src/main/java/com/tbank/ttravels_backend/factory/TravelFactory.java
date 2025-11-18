package com.tbank.ttravels_backend.factory;

import com.tbank.ttravels_backend.dto.travel.CreateTravelRequest;
import com.tbank.ttravels_backend.entity.Travel;
import com.tbank.ttravels_backend.entity.TravelMember;
import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.enums.MemberRole;
import com.tbank.ttravels_backend.enums.MemberStatus;
import com.tbank.ttravels_backend.enums.TravelStatus;
import org.springframework.stereotype.Component;

@Component
public class TravelFactory {

    public Travel createTravel(CreateTravelRequest request, User owner) {
        return Travel.builder()
                .name(request.getName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(TravelStatus.ACTIVE)
                .owner(owner)
                .build();
    }

    public TravelMember ownerMembership(Travel travel, User owner) {
        return TravelMember.builder()
                .travel(travel)
                .user(owner)
                .status(MemberStatus.ACCEPTED)
                .role(MemberRole.OWNER)
                .build();
    }

    public TravelMember invitedMember(Travel travel, User invitedUser) {
        return TravelMember.builder()
                .travel(travel)
                .user(invitedUser)
                .status(MemberStatus.INVITED)
                .role(MemberRole.MEMBER)
                .build();
    }
}
