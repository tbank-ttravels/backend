package com.tbank.ttravels_backend.repository;

import com.tbank.ttravels_backend.entity.TravelMember;
import com.tbank.ttravels_backend.enums.MemberRole;
import com.tbank.ttravels_backend.enums.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelMemberRepository extends JpaRepository<TravelMember, Long> {

    List<TravelMember> findByTravelId(Long travelId);

    List<TravelMember> findByUserId(Long userId);

    boolean existsByUserIdAndTravelId(Long userId, Long travelId);

    boolean existsByUserIdAndTravelIdAndRole(Long userId, Long travelId, MemberRole role);

    List<TravelMember> findAllByUserIdAndStatus(Long userId, MemberStatus memberStatus);
}
