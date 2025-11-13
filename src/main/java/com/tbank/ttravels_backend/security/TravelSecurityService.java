package com.tbank.ttravels_backend.security;

import com.tbank.ttravels_backend.enums.MemberRole;
import com.tbank.ttravels_backend.repository.TravelMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("travelSecurity")
@RequiredArgsConstructor
public class TravelSecurityService {
    private final TravelMemberRepository travelMemberRepository;

    public boolean isMember(Long travelId, Long userId) {
        return travelMemberRepository.existsByUserIdAndTravelId(userId, travelId);
    }

    public boolean isOwner(Long travelId, Long userId) {
        return travelMemberRepository.existsByUserIdAndTravelIdAndRole(userId, travelId, MemberRole.OWNER);
    }
}