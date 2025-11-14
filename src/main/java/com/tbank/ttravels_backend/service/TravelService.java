package com.tbank.ttravels_backend.service;

import com.tbank.ttravels_backend.dto.travel.*;
import com.tbank.ttravels_backend.entity.Travel;
import com.tbank.ttravels_backend.entity.TravelMember;
import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.enums.MemberRole;
import com.tbank.ttravels_backend.enums.MemberStatus;
import com.tbank.ttravels_backend.enums.TravelStatus;
import com.tbank.ttravels_backend.exception.*;
import com.tbank.ttravels_backend.repository.TravelMemberRepository;
import com.tbank.ttravels_backend.repository.TravelRepository;
import com.tbank.ttravels_backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TravelService {
    private final UserRepository userRepository;
    private final TravelRepository travelRepository;
    private final TravelMemberRepository travelMemberRepository;

    @Transactional
    public TravelResponse createTravel(CreateTravelRequest request, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        Travel newTravel = Travel.builder()
                .name(request.getName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(TravelStatus.ACTIVE)
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

        return new TravelResponse(
                savedTravel.getId(),
                savedTravel.getName(),
                savedTravel.getDescription(),
                savedTravel.getStartDate(),
                savedTravel.getEndDate(),
                savedTravel.getStatus());
    }

    @Transactional
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
    public TravelResponse getTravel(Long travelId) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new TravelNotFoundException("Поездка не найдена"));

        return new TravelResponse(
                travel.getId(),
                travel.getName(),
                travel.getDescription(),
                travel.getStartDate(),
                travel.getEndDate(),
                travel.getStatus());

    }

    @Transactional
    public TravelResponse editTravel(Long travelId, EditTravelRequest request) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new TravelNotFoundException("Поездка не найдена"));

        OffsetDateTime updatedStart = request.getStartDate() != null ? request.getStartDate() : travel.getStartDate();
        OffsetDateTime updatedEnd = request.getEndDate() != null ? request.getEndDate() : travel.getEndDate();

        if (updatedStart != null && updatedEnd != null && !updatedEnd.isAfter(updatedStart)) {
            throw new InvalidDateRangeException("Дата окончания должна быть позже даты начала");
        }

        if (request.getName() != null) {
            travel.setName(request.getName());
        }
        if (request.getDescription() != null) {
            travel.setDescription(request.getDescription());
        }
        if (request.getStartDate() != null) {
            travel.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            travel.setEndDate(request.getEndDate());
        }

        Travel updatedTravel = travelRepository.save(travel);

        return new TravelResponse(
                updatedTravel.getId(),
                updatedTravel.getName(),
                updatedTravel.getDescription(),
                updatedTravel.getStartDate(),
                updatedTravel.getEndDate(),
                updatedTravel.getStatus());
    }

    @Transactional
    public void closeTravel(Long travelId) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new TravelNotFoundException("Поездка не найдена"));

        if (travel.getStatus() == TravelStatus.CLOSED) {
            throw new ConflictStateException("Поездка уже закрыта");
        }

        travel.setStatus(TravelStatus.CLOSED);
        travelRepository.save(travel);
    }

    @Transactional
    public void reopenTravel(Long travelId) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new TravelNotFoundException("Поездка не найдена"));

        if (travel.getStatus() == TravelStatus.ACTIVE) {
            throw new ConflictStateException("Поездка уже открыта");
        }

        travel.setStatus(TravelStatus.ACTIVE);
        travelRepository.save(travel);
    }

    @Transactional
    public void deleteTravel(Long travelId) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new TravelNotFoundException("Поездка не найдена"));

        travelRepository.delete(travel);
    }

    @Transactional
    public void inviteMember(Long travelId, String phone) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new TravelNotFoundException("Поездка не найдена"));

        User invitedUser = userRepository.findByPhone(phone)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        if (travelMemberRepository.existsByTravelIdAndUserId(travelId, invitedUser.getId())) {
            return;
        }

        TravelMember newMember = TravelMember.builder()
                .travel(travel)
                .user(invitedUser)
                .status(MemberStatus.INVITED)
                .role(MemberRole.MEMBER)
                .build();
        travelMemberRepository.save(newMember);
    }

    @Transactional
    public InvitesResponse getInvites(Long userId) {
        List<TravelMember> invites = travelMemberRepository
                .findAllByUserIdAndStatus(userId, MemberStatus.INVITED);

        List<InvitesItem> items = invites.stream()
                .map(invite -> {
                    Travel travel = invite.getTravel();
                    return new InvitesItem(
                            invite.getId(),
                            travel.getName(),
                            travel.getDescription(),
                            travel.getStartDate(),
                            travel.getEndDate()
                    );
                })
                .toList();


        return new InvitesResponse(items);
    }

    @Transactional
    public void respondToInvite(Long travelId, Long userId, boolean accept) {
        TravelMember invite = travelMemberRepository.findByTravelIdAndUserIdAndStatus(travelId, userId, MemberStatus.INVITED)
                .orElseThrow(() -> new TravelNotFoundException("Приглашение не найдено"));

        if (accept) {
            invite.setStatus(MemberStatus.ACCEPTED);
        } else {
            invite.setStatus(MemberStatus.REJECTED);
        }

        travelMemberRepository.save(invite);
    }

    @Transactional
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

    @Transactional
    public void kickMember(Long travelId, Long userId) {
        TravelMember member = travelMemberRepository.findByTravelIdAndUserId(travelId, userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден в поездке"));

        if (member.getRole() == MemberRole.OWNER) {
            throw new OwnerRemovalNotAllowedException("Нельзя исключить владельца поездки");
        }

        travelMemberRepository.delete(member);
    }

    @Transactional
    public void leaveTravel(Long travelId, Long userId) {
        TravelMember member = travelMemberRepository.findByTravelIdAndUserId(travelId, userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден в поездке"));

        if (member.getRole() == MemberRole.OWNER) {
            throw new OwnerRemovalNotAllowedException("Владелец не может покинуть поездку");
        }

        travelMemberRepository.delete(member);
    }

}
