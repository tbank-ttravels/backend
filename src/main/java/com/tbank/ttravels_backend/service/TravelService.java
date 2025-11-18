package com.tbank.ttravels_backend.service;

import com.tbank.ttravels_backend.dto.travel.*;
import com.tbank.ttravels_backend.entity.Travel;
import com.tbank.ttravels_backend.entity.TravelMember;
import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.enums.MemberRole;
import com.tbank.ttravels_backend.enums.MemberStatus;
import com.tbank.ttravels_backend.enums.TravelStatus;
import com.tbank.ttravels_backend.exception.*;
import com.tbank.ttravels_backend.factory.TravelFactory;
import com.tbank.ttravels_backend.mapper.TravelMapper;
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
    private final TravelMapper travelMapper;

    @Transactional
    public TravelResponse createTravel(CreateTravelRequest request, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Travel travel = travelRepository.save(TravelFactory.createTravel(request, owner));
        travelMemberRepository.save(TravelFactory.ownerMembership(travel, owner));

        return travelMapper.toTravelResponse(travel);
    }

    @Transactional
    public MyTravelsResponse getMyTravels(Long userId) {
        List<Travel> travels = travelMemberRepository.findAllByUserIdAndStatus(userId, MemberStatus.ACCEPTED)
                .stream()
                .map(TravelMember::getTravel)
                .toList();
        return travelMapper.toMyTravelsResponse(travels);
    }

    @Transactional
    public TravelResponse getTravel(Long travelId) {
        Travel travel = findTravel(travelId);
        return travelMapper.toTravelResponse(travel);
    }

    @Transactional
    public TravelResponse editTravel(Long travelId, EditTravelRequest request) {
        Travel travel = findTravel(travelId);

        OffsetDateTime updatedStart = request.getStartDate() != null ? request.getStartDate() : travel.getStartDate();
        OffsetDateTime updatedEnd = request.getEndDate() != null ? request.getEndDate() : travel.getEndDate();
        validateDateRange(updatedStart, updatedEnd);

        applyUpdates(travel, request);
        Travel updated = travelRepository.save(travel);

        return travelMapper.toTravelResponse(updated);
    }

    @Transactional
    public void closeTravel(Long travelId) {
        Travel travel = findTravel(travelId);
        if (travel.getStatus() == TravelStatus.CLOSED) {
            throw new ConflictStateException("Поездка уже закрыта");
        }
        travel.setStatus(TravelStatus.CLOSED);
        travelRepository.save(travel);
    }

    @Transactional
    public void reopenTravel(Long travelId) {
        Travel travel = findTravel(travelId);
        if (travel.getStatus() == TravelStatus.ACTIVE) {
            throw new ConflictStateException("Поездка уже открыта");
        }
        travel.setStatus(TravelStatus.ACTIVE);
        travelRepository.save(travel);
    }

    @Transactional
    public void deleteTravel(Long travelId) {
        Travel travel = findTravel(travelId);
        travelRepository.delete(travel);
    }

    @Transactional
    public void inviteMembers(Long travelId, List<String> phones) {
        if (phones == null || phones.isEmpty()) {
            return;
        }

        Travel travel = findTravel(travelId);

        phones.stream()
                .map(String::trim)
                .distinct()
                .forEach(phone -> inviteSingleMember(travel, phone));
    }

    @Transactional
    public InvitesResponse getInvites(Long userId) {
        List<TravelMember> invites = travelMemberRepository
                .findAllByUserIdAndStatus(userId, MemberStatus.INVITED);
        return travelMapper.toInvitesResponse(invites);
    }

    @Transactional
    public void respondToInvite(Long travelId, Long userId, boolean accept) {
        TravelMember invite = travelMemberRepository
                .findByTravelIdAndUserIdAndStatus(travelId, userId, MemberStatus.INVITED)
                .orElseThrow(() -> new UserNotFoundInTravelException(travelId, userId));

        invite.setStatus(accept ? MemberStatus.ACCEPTED : MemberStatus.REJECTED);
        travelMemberRepository.save(invite);
    }

    @Transactional
    public TravelMembersResponse getTravelMembers(Long travelId) {
        List<TravelMember> members = travelMemberRepository.findByTravelId(travelId);
        return travelMapper.toMembersResponse(members);
    }

    @Transactional
    public void kickMember(Long travelId, Long userId) {
        TravelMember member = travelMemberRepository.findByTravelIdAndUserId(travelId, userId)
                .orElseThrow(() -> new UserNotFoundInTravelException(travelId, userId));

        if (member.getRole() == MemberRole.OWNER) {
            throw new OwnerRemovalNotAllowedException("Нельзя исключить владельца поездки");
        }

        travelMemberRepository.delete(member);
    }

    @Transactional
    public void leaveTravel(Long travelId, Long userId) {
        TravelMember member = travelMemberRepository.findByTravelIdAndUserId(travelId, userId)
                .orElseThrow(() -> new UserNotFoundInTravelException(travelId, userId));

        if (member.getRole() == MemberRole.OWNER) {
            throw new OwnerRemovalNotAllowedException("Владелец не может покинуть поездку");
        }

        travelMemberRepository.delete(member);
    }

    private void applyUpdates(Travel travel, EditTravelRequest request) {
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
    }


    public Travel findTravel(Long travelId) {
        return travelRepository.findById(travelId)
                .orElseThrow(() -> new TravelNotFoundException(travelId));
    }

    private void validateDateRange(OffsetDateTime start, OffsetDateTime end) {
        if (start != null && end != null && !end.isAfter(start)) {
            throw new InvalidDateRangeException("Дата окончания должна быть позже даты начала");
        }
    }

    private void inviteSingleMember(Travel travel, String phone) {
        User invitedUser = userRepository.findByPhone(phone)
                .orElseThrow(() -> new UserNotFoundByPhoneException(phone));

        if (travelMemberRepository.existsByTravelIdAndUserId(travel.getId(), invitedUser.getId())) {
            throw new ConflictStateException("Пользователь %s уже приглашён или является участником поездки"
                    .formatted(phone));
        }

        TravelMember newMember = TravelFactory.invitedMember(travel, invitedUser);
        travelMemberRepository.save(newMember);
    }
}
