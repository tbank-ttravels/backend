package com.tbank.ttravels_backend.service;

import com.tbank.ttravels_backend.entity.Travel;
import com.tbank.ttravels_backend.enums.MemberRole;
import com.tbank.ttravels_backend.enums.MemberStatus;
import com.tbank.ttravels_backend.enums.TravelStatus;
import com.tbank.ttravels_backend.repository.TravelMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("travelSecurity")
@RequiredArgsConstructor
public class TravelSecurityService {
    private final TravelService travelService;
    private final TravelMemberRepository travelMemberRepository;

    public boolean isInvited(Long inviteId, Long userId) {
        return travelMemberRepository.existsByIdAndUserIdAndStatus(inviteId, userId, MemberStatus.INVITED);
    }

    public boolean isMember(Long travelId, Long userId) {

        return travelMemberRepository.existsByUserIdAndTravelIdAndRoleAndStatus(userId, travelId, MemberRole.MEMBER, MemberStatus.ACCEPTED) ||
                travelMemberRepository.existsByUserIdAndTravelIdAndRoleAndStatus(userId, travelId, MemberRole.OWNER, MemberStatus.ACCEPTED);
    }

    public boolean isTravelOpen(Long travelId) {
        Travel travel = travelService.findTravel(travelId);
        return travel.getStatus() == TravelStatus.ACTIVE;
    }

    public boolean isOwner(Long travelId, Long userId) {
        return travelMemberRepository.existsByUserIdAndTravelIdAndRole(userId, travelId, MemberRole.OWNER);
    }
}