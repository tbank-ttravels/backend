package com.tbank.ttravels_backend.service;

import com.tbank.ttravels_backend.dto.travel.MyTravelsResponse;
import com.tbank.ttravels_backend.dto.travel.member.InvitesResponse;
import com.tbank.ttravels_backend.dto.travel.member.TravelMembersResponse;
import com.tbank.ttravels_backend.entity.Travel;
import com.tbank.ttravels_backend.entity.TravelMember;
import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.enums.MemberRole;
import com.tbank.ttravels_backend.enums.MemberStatus;
import com.tbank.ttravels_backend.exception.ConflictStateException;
import com.tbank.ttravels_backend.exception.InviteNotFoundException;
import com.tbank.ttravels_backend.exception.OwnerRemovalNotAllowedException;
import com.tbank.ttravels_backend.exception.UserNotFoundInTravelException;
import com.tbank.ttravels_backend.factory.TravelMemberFactory;
import com.tbank.ttravels_backend.mapper.TravelMapper;
import com.tbank.ttravels_backend.mapper.TravelMemberMapper;
import com.tbank.ttravels_backend.repository.TravelMemberRepository;
import com.tbank.ttravels_backend.security.AccountService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TravelMemberService {

    private final TravelMemberRepository travelMemberRepository;
    private final TravelMemberMapper travelMemberMapper;
    private final AccountService accountService;
    private final TravelService travelService;
    private final TravelMapper travelMapper;

    @Transactional
    public void inviteMembers(Long travelId, List<String> phones) {
        if (phones == null || phones.isEmpty()) {
            return;
        }

        Travel travel = travelService.findTravel(travelId);

        phones.stream()
                .map(String::trim)
                .distinct()
                .forEach(phone -> inviteSingleMember(travel, phone));
    }

    @Transactional
    public MyTravelsResponse getMyTravels(Long userId) {
        List<Travel> travels = findAllTravelForUser(userId);
        return travelMapper.toMyTravelsResponse(travels);
    }

    @Transactional
    public InvitesResponse getInvites(Long userId) {
        List<TravelMember> invites = findInviteInTravelForUser(userId);
        return travelMemberMapper.toInvitesResponse(invites);
    }

    @Transactional
    public void respondToInvite(Long inviteId, Long userId, boolean accept) {
        TravelMember invite = travelMemberRepository.findByIdAndUserIdAndStatus(inviteId, userId, MemberStatus.INVITED)
                .orElseThrow(() -> new InviteNotFoundException("Для пользователя с id = " + userId +
                        " не найдено приглашение с id = " + inviteId));

        invite.setStatus(accept ? MemberStatus.ACCEPTED : MemberStatus.REJECTED);
        saveTravelMember(invite);
    }

    @Transactional
    public TravelMembersResponse getTravelMembers(Long travelId) {
        List<TravelMember> members = findAllMembersInTravel(travelId);
        return travelMemberMapper.toMembersResponse(members);
    }

    @Transactional
    public void kickMember(Long travelId, Long userId) {
        TravelMember member = findMemberInTravel(userId, travelId);

        if (member.getRole() == MemberRole.OWNER) {
            throw new OwnerRemovalNotAllowedException("Нельзя исключить владельца поездки");
        }

        deleteTravelMember(member);
    }

    @Transactional
    public void leaveTravel(Long travelId, Long userId) {
        TravelMember member = findMemberInTravel(userId, travelId);

        if (member.getRole() == MemberRole.OWNER) {
            throw new OwnerRemovalNotAllowedException("Владелец не может покинуть поездку");
        }

        deleteTravelMember(member);
    }

    private void inviteSingleMember(Travel travel, String phone) {
        User invitedUser = accountService.findUserByPhone(phone);

        if (travelMemberRepository.existsByTravelIdAndUserId(travel.getId(), invitedUser.getId())) {
            throw new ConflictStateException("Пользователь %s уже приглашён или является участником поездки"
                    .formatted(phone));
        }

        TravelMember newMember = TravelMemberFactory.createInvitedMember(travel, invitedUser);
        travelService.addTravelMember(travel, newMember);
        travelService.saveTravel(travel);
    }

    public TravelMember saveTravelMember(TravelMember travelMember) {
        return travelMemberRepository.save(travelMember);
    }

    private void deleteTravelMember(TravelMember travelMember) {
        Travel travel = travelMember.getTravel();
        travelService.removeTravelMember(travel, travelMember);
        travelService.saveTravel(travel);
    }

    public List<Travel> findAllTravelForUser(Long userId) {
        return travelMemberRepository.findAllByUserIdAndStatus(userId, MemberStatus.ACCEPTED)
                .stream()
                .map(TravelMember::getTravel)
                .toList();
    }

    public User findUserInTravel(Long userId, Long travelId) {
        return travelMemberRepository.findByUserIdAndTravelId(userId, travelId)
                .orElseThrow(() ->
                        new UserNotFoundInTravelException("Пользователь с id = " + userId +
                                " не найден в поездке с id = " + travelId))
                .getUser();
    }

    public void checkUserInTravel(Long userId, Long travelId) {
        if (!travelMemberRepository.existsByUserIdAndTravelId(userId, travelId))
            throw new UserNotFoundInTravelException("Пользователь с id = " + userId +
                    " не найден в поездке с id = " + travelId);
    }

    public void validateAllUsersInTravel(Long travelId, Set<Long> participantUserIds) {

        if (participantUserIds == null) return;

        participantUserIds.forEach(userId -> checkUserInTravel(userId, travelId));
    }

    public List<TravelMember> findAllMembersInTravel(Long travelId) {
        return travelMemberRepository.findByTravelId(travelId);
    }

    public TravelMember findMemberInTravel(Long userId, Long travelId) {
        return travelMemberRepository.findByUserIdAndTravelId(userId, travelId)
                .orElseThrow(() -> new UserNotFoundInTravelException("Пользователь с id = " + userId +
                        " не найден в поездке с id = " + travelId));
    }

    public List<TravelMember> findInviteInTravelForUser(Long userId) {
        return travelMemberRepository.findAllByUserIdAndStatus(userId, MemberStatus.INVITED);
    }
}
