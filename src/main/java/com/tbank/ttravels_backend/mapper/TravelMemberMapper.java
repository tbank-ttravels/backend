package com.tbank.ttravels_backend.mapper;

import com.tbank.ttravels_backend.dto.travel.member.InvitesItem;
import com.tbank.ttravels_backend.dto.travel.member.InvitesResponse;
import com.tbank.ttravels_backend.dto.travel.member.TravelMemberItem;
import com.tbank.ttravels_backend.dto.travel.member.TravelMembersResponse;
import com.tbank.ttravels_backend.entity.Travel;
import com.tbank.ttravels_backend.entity.TravelMember;
import com.tbank.ttravels_backend.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TravelMemberMapper {

    public InvitesItem toInviteItem(TravelMember invite) {
        Travel travel = invite.getTravel();
        return new InvitesItem(
                invite.getId(),
                travel.getName(),
                travel.getDescription(),
                travel.getStartDate(),
                travel.getEndDate()
        );
    }

    public InvitesResponse toInvitesResponse(List<TravelMember> invites) {
        List<InvitesItem> items = invites.stream()
                .map(this::toInviteItem)
                .collect(Collectors.toList());
        return new InvitesResponse(items);
    }

    public TravelMemberItem toMemberItem(TravelMember member) {
        User user = member.getUser();
        return new TravelMemberItem(
                user.getId(),
                user.getName(),
                user.getPhone(),
                member.getStatus()
        );
    }

    public TravelMembersResponse toMembersResponse(List<TravelMember> members) {
        List<TravelMemberItem> items = members.stream()
                .map(this::toMemberItem)
                .collect(Collectors.toList());
        return new TravelMembersResponse(items);
    }
}
