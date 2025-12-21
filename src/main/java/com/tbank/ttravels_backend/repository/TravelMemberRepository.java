package com.tbank.ttravels_backend.repository;

import com.tbank.ttravels_backend.entity.TravelMember;
import com.tbank.ttravels_backend.enums.MemberRole;
import com.tbank.ttravels_backend.enums.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TravelMemberRepository extends JpaRepository<TravelMember, Long> {

    List<TravelMember> findByTravelId(Long travelId);

    boolean existsByUserIdAndTravelId(Long userId, Long travelId);

    Optional<TravelMember> findByUserIdAndTravelId(Long userId, Long travelId);
  
    boolean existsByUserIdAndTravelIdAndRoleAndStatus(Long userId, Long travelId, MemberRole role, MemberStatus status);

    boolean existsByUserIdAndTravelIdAndRole(Long userId, Long travelId, MemberRole role);

    boolean existsByTravelIdAndUserIdAndStatus(Long travelId, Long userId, MemberStatus status);

    boolean existsByIdAndUserIdAndStatus(Long id, Long userId, MemberStatus status);

    List<TravelMember> findAllByUserIdAndStatus(Long userId, MemberStatus memberStatus);

    boolean existsByTravelIdAndUserId(Long travelId, Long id);

    Optional<TravelMember> findByIdAndUserIdAndStatus(Long Id, Long userId, MemberStatus status);

    Optional<TravelMember> findByUserIdAndTravelIdAndStatus(Long userId, Long travelId, MemberStatus status);
}
