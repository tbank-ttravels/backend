package com.tbank.ttravels_backend.service;

import com.tbank.ttravels_backend.dto.travel.*;
import com.tbank.ttravels_backend.entity.Travel;
import com.tbank.ttravels_backend.entity.TravelMember;
import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.enums.MemberRole;
import com.tbank.ttravels_backend.enums.MemberStatus;
import com.tbank.ttravels_backend.exception.TravelNotFound;
import com.tbank.ttravels_backend.exception.UserNotFound;
import com.tbank.ttravels_backend.repository.TravelMemberRepository;
import com.tbank.ttravels_backend.repository.TravelRepository;
import com.tbank.ttravels_backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TravelService {
    private final UserRepository userRepository;
    private final TravelRepository travelRepository;
    private final TravelMemberRepository travelMemberRepository;

    @Transactional
    public CreateTravelResponse createTravel(CreateTravelRequest request, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFound("Пользователь не найден"));

        Travel newTravel = Travel.builder()
                .name(request.getName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .owner(owner)
                .build();
        Travel savedTravel = travelRepository.save(newTravel);

        TravelMember ownerMember = TravelMember.builder()
                .travel(savedTravel)
                .user(owner)
                .status(MemberStatus.ACCEPTED)
                .role(MemberRole.OWNER)
                .build();
        travelMemberRepository.save(ownerMember);

        return new CreateTravelResponse(
                savedTravel.getId(),
                savedTravel.getName(),
                savedTravel.getDescription(),
                savedTravel.getStartDate(),
                savedTravel.getEndDate());
    }

    public MyTravelsResponse getMyTravels(Long userId) {
        List<TravelMember> memberships = travelMemberRepository
                .findAllByUserIdAndStatus(userId, MemberStatus.ACCEPTED);

        List<MyTravelItem> items = memberships.stream()
                .map(member -> {
                    Travel travel = member.getTravel();
                    return new MyTravelItem(
                            travel.getId(),
                            travel.getName(),
                            travel.getDescription(),
                            travel.getStartDate(),
                            travel.getEndDate(),
                            travel.getStatus()
                    );
                })
                .toList();

        return new MyTravelsResponse(items);
    }


    @Transactional
    public void inviteMember(Long travelId, String phone) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new TravelNotFound("Поездка не найдена"));

        User invitedUser = userRepository.findByPhone(phone)
                .orElseThrow(() -> new UserNotFound("Пользователь не найден"));

        TravelMember newMember = TravelMember.builder()
                .travel(travel)
                .user(invitedUser)
                .status(MemberStatus.INVITED)
                .role(MemberRole.MEMBER)
                .build();
        travelMemberRepository.save(newMember);
    }

    public TravelMembersResponse getTravelMembers(Long travelId) {
        List<TravelMember> members = travelMemberRepository.findByTravelId(travelId);

        List<TravelMemberItem> items = members.stream()
                .map(member -> {
                    User user = member.getUser();
                    return new TravelMemberItem(
                            user.getId(),
                            user.getName(),
                            user.getPhone(),
                            member.getStatus()
                    );
                })
                .toList();

        return new TravelMembersResponse(items);
    }

}
