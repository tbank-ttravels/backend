package com.tbank.ttravels_backend.repository;

import com.tbank.ttravels_backend.entity.TravelMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelMemberRepository extends JpaRepository<TravelMember, Long> {

    List<TravelMember> findByTravelId(Long travelId);

    List<TravelMember> findByUserId(Long userId);

    boolean existsByUserIdAndTravelId(Long userId, Long travelId);
}
