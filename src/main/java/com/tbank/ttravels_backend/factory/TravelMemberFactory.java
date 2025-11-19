package com.tbank.ttravels_backend.factory;

import com.tbank.ttravels_backend.entity.Travel;
import com.tbank.ttravels_backend.entity.TravelMember;
import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.enums.MemberRole;
import com.tbank.ttravels_backend.enums.MemberStatus;

public class TravelMemberFactory {

    private TravelMemberFactory() {
        throw new UnsupportedOperationException();
    }

    public static TravelMember ownerMembership(Travel travel, User owner) {
        return TravelMember.builder()
                .travel(travel)
                .user(owner)
                .status(MemberStatus.ACCEPTED)
                .role(MemberRole.OWNER)
                .build();
    }

    public static TravelMember invitedMember(Travel travel, User invitedUser) {
        return TravelMember.builder()
                .travel(travel)
                .user(invitedUser)
                .status(MemberStatus.INVITED)
                .role(MemberRole.MEMBER)
                .build();
    }
}
