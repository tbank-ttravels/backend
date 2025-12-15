package com.tbank.ttravels_backend.service.Travel;

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
import com.tbank.ttravels_backend.mapper.TravelMapper;
import com.tbank.ttravels_backend.mapper.TravelMemberMapper;
import com.tbank.ttravels_backend.repository.TravelMemberRepository;
import com.tbank.ttravels_backend.service.AccountService;
import com.tbank.ttravels_backend.service.TestDataFactory;
import com.tbank.ttravels_backend.service.TravelMemberService;
import com.tbank.ttravels_backend.service.TravelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TravelMemberServiceTest {

    @Mock
    private TravelMemberRepository travelMemberRepository;
    @Mock
    private TravelMemberMapper travelMemberMapper;
    @Mock
    private AccountService accountService;
    @Mock
    private TravelService travelService;
    @Mock
    private TravelMapper travelMapper;

    @InjectMocks
    private TravelMemberService travelMemberService;

    @Captor
    private ArgumentCaptor<TravelMember> memberCaptor;


    @Test
    void inviteMembers_shouldReturnWhenPhonesEmpty() {

        long travelId = 1;

        travelMemberService.inviteMembers(travelId, List.of());

        verifyNoInteractions(travelService, accountService, travelMemberRepository);
    }


    @Test
    void inviteMembers_shouldInviteDistinctTrimmedPhones() {

        long travelId = 1;
        Travel travel = TestDataFactory.travel(travelId);
        String phone1 = " +78001112233 ";
        String phone2 = "+78001112233";
        String phone3 = "+79009998877";

        long invitedId1 = 1, invitedId2 = 2;
        User invited1 = TestDataFactory.user(invitedId1, phone1.trim());
        User invited2 = TestDataFactory.user(invitedId2, phone3);

        when(travelService.findTravel(travelId)).thenReturn(travel);
        when(accountService.findUserByPhone(phone1.trim())).thenReturn(invited1);
        when(accountService.findUserByPhone(phone3)).thenReturn(invited2);
        when(travelMemberRepository.existsByTravelIdAndUserId(travelId, invitedId1)).thenReturn(false);
        when(travelMemberRepository.existsByTravelIdAndUserId(travelId, invitedId2)).thenReturn(false);

        travelMemberService.inviteMembers(travelId, List.of(phone1, phone2, phone3));

        verify(travelService, times(2)).addTravelMember(eq(travel), memberCaptor.capture());
        verify(travelService, times(2)).saveTravel(travel);
        List<TravelMember> created = memberCaptor.getAllValues();
        assertThat(created)
                .hasSize(2)
                .allSatisfy(inv -> {
                    assertThat(inv.getTravel()).isEqualTo(travel);
                    assertThat(inv.getStatus()).isEqualTo(MemberStatus.INVITED);
                    assertThat(inv.getRole()).isEqualTo(MemberRole.MEMBER);
                });
        assertThat(created).extracting(m -> m.getUser().getId()).containsExactlyInAnyOrder(invitedId1, invitedId2);
    }

    @Test
    void inviteMembers_shouldThrowWhenConflict() {

        long travelId = 1, invitedId = 1;
        Travel travel = TestDataFactory.travel(travelId);
        String phone = "+78001112233";
        User invited = TestDataFactory.user(invitedId, phone);

        when(travelService.findTravel(travelId)).thenReturn(travel);
        when(accountService.findUserByPhone(phone)).thenReturn(invited);
        when(travelMemberRepository.existsByTravelIdAndUserId(travelId, invitedId)).thenReturn(true);

        assertThatThrownBy(() -> travelMemberService.inviteMembers(travelId, List.of(phone)))
                .isInstanceOf(ConflictStateException.class);

        verify(travelService, never()).addTravelMember(any(), any());
        verify(travelService, never()).saveTravel(any());
    }


    @Test
    void getMyTravels_shouldMapResponse() {

        long travelId = 1, travelMemberId = 2, userId = 3;
        TravelMember tm = TestDataFactory.travelMember(travelMemberId, userId, travelId);

        when(travelMemberRepository.findAllByUserIdAndStatus(userId, MemberStatus.ACCEPTED))
                .thenReturn(List.of(tm));
        MyTravelsResponse expected = new MyTravelsResponse(List.of());
        when(travelMapper.toMyTravelsResponse(List.of(tm.getTravel()))).thenReturn(expected);

        MyTravelsResponse response = travelMemberService.getMyTravels(userId);

        assertThat(response).isEqualTo(expected);
        verify(travelMemberRepository).findAllByUserIdAndStatus(userId, MemberStatus.ACCEPTED);
        verify(travelMapper).toMyTravelsResponse(List.of(tm.getTravel()));
    }

    @Test
    void getInvites_shouldMapResponse() {

        long travelMemberId = 2, userId = 3;
        TravelMember invite = TestDataFactory.travelMemberInvite(travelMemberId, userId);
        InvitesResponse expected = new InvitesResponse(List.of());

        when(travelMemberRepository.findAllByUserIdAndStatus(userId, MemberStatus.INVITED))
                .thenReturn(List.of(invite));
        when(travelMemberMapper.toInvitesResponse(List.of(invite))).thenReturn(expected);

        InvitesResponse response = travelMemberService.getInvites(userId);

        assertThat(response).isEqualTo(expected);
        verify(travelMemberRepository).findAllByUserIdAndStatus(userId, MemberStatus.INVITED);
        verify(travelMemberMapper).toInvitesResponse(List.of(invite));
    }

    @Test
    void respondToInvite_shouldAccept() {

        long travelMemberId = 2, userId = 3;
        TravelMember invite = TestDataFactory.travelMemberInvite(travelMemberId, userId);

        when(travelMemberRepository.findByIdAndUserIdAndStatus(travelMemberId, userId, MemberStatus.INVITED))
                .thenReturn(Optional.of(invite));
        when(travelMemberRepository.save(any(TravelMember.class))).thenAnswer(inv -> inv.getArgument(0));

        travelMemberService.respondToInvite(travelMemberId, userId, true);

        assertThat(invite.getStatus()).isEqualTo(MemberStatus.ACCEPTED);
        verify(travelMemberRepository).save(invite);
    }

    @Test
    void respondToInvite_shouldReject() {

        long travelMemberId = 2, userId = 3;
        TravelMember invite = TestDataFactory.travelMemberInvite(travelMemberId, userId);

        when(travelMemberRepository.findByIdAndUserIdAndStatus(travelMemberId, userId, MemberStatus.INVITED))
                .thenReturn(Optional.of(invite));

        travelMemberService.respondToInvite(travelMemberId, userId, false);

        assertThat(invite.getStatus()).isEqualTo(MemberStatus.REJECTED);
        verify(travelMemberRepository).save(invite);
    }

    @Test
    void respondToInvite_shouldThrowWhenNotFound() {

        long travelMemberId = 2, userId = 3;

        when(travelMemberRepository.findByIdAndUserIdAndStatus(travelMemberId, userId, MemberStatus.INVITED))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> travelMemberService.respondToInvite(travelMemberId, userId, true))
                .isInstanceOf(InviteNotFoundException.class);

        verify(travelMemberRepository, never()).save(any());
    }

    @Test
    void getTravelMembers_shouldReturnMapped() {

        long travelId = 1, travelMemberId = 2, userId = 3;
        TravelMember tm = TestDataFactory.travelMember(travelMemberId, userId, travelId);
        TravelMembersResponse expected = new TravelMembersResponse(List.of());

        when(travelMemberRepository.findByTravelId(travelId)).thenReturn(List.of(tm));
        when(travelMemberMapper.toMembersResponse(List.of(tm))).thenReturn(expected);

        TravelMembersResponse response = travelMemberService.getTravelMembers(travelId);

        assertThat(response).isEqualTo(expected);
        verify(travelMemberRepository).findByTravelId(travelId);
        verify(travelMemberMapper).toMembersResponse(List.of(tm));
    }

    @Test
    void kickMember_shouldMarkMemberAsLeft() {

        long travelId = 1, travelMemberId = 2, userId = 3;
        TravelMember toKick = TestDataFactory.travelMember(
                travelMemberId, userId, travelId, MemberRole.MEMBER, MemberStatus.ACCEPTED);

        when(travelMemberRepository.findByUserIdAndTravelId(userId, travelId)).thenReturn(Optional.of(toKick));

        travelMemberService.kickMember(travelId, userId);

        assertThat(toKick.getStatus()).isEqualTo(MemberStatus.LEAVE);
        verify(travelMemberRepository).findByUserIdAndTravelId(userId, travelId);
        verifyNoInteractions(travelService);
    }


    @Test
    void kickMember_shouldThrowWhenOwner() {

        long travelId = 1, travelMemberId = 2, userId = 3;
        TravelMember ownerMember = TestDataFactory.travelMember(
                travelMemberId, userId, travelId, MemberRole.OWNER, MemberStatus.ACCEPTED);

        when(travelMemberRepository.findByUserIdAndTravelId(userId, travelId)).thenReturn(Optional.of(ownerMember));

        assertThatThrownBy(() -> travelMemberService.kickMember(travelId, userId))
                .isInstanceOf(OwnerRemovalNotAllowedException.class);

        assertThat(ownerMember.getStatus()).isEqualTo(MemberStatus.ACCEPTED);
        verify(travelMemberRepository).findByUserIdAndTravelId(userId, travelId);
        verifyNoInteractions(travelService);
    }

    @Test
    void leaveTravel_shouldMarkMemberAsLeft() {

        long travelId = 1, travelMemberId = 2, userId = 3;
        TravelMember toLeave = TestDataFactory.travelMember(
                travelMemberId, userId, travelId, MemberRole.MEMBER, MemberStatus.ACCEPTED);

        when(travelMemberRepository.findByUserIdAndTravelId(userId, travelId)).thenReturn(Optional.of(toLeave));

        travelMemberService.leaveTravel(travelId, userId);

        assertThat(toLeave.getStatus()).isEqualTo(MemberStatus.LEAVE);
        verify(travelMemberRepository).findByUserIdAndTravelId(userId, travelId);
        verifyNoInteractions(travelService);
    }

    @Test
    void leaveTravel_shouldThrowWhenOwner() {

        long travelId = 1, travelMemberId = 2, userId = 3;
        TravelMember ownerMember = TestDataFactory.travelMember(
                travelMemberId, userId, travelId, MemberRole.OWNER, MemberStatus.ACCEPTED);

        when(travelMemberRepository.findByUserIdAndTravelId(userId, travelId)).thenReturn(Optional.of(ownerMember));

        assertThatThrownBy(() -> travelMemberService.leaveTravel(travelId, userId))
                .isInstanceOf(OwnerRemovalNotAllowedException.class);

        assertThat(ownerMember.getStatus()).isEqualTo(MemberStatus.ACCEPTED);
        verify(travelMemberRepository).findByUserIdAndTravelId(userId, travelId);
        verifyNoInteractions(travelService);
    }

    @Test
    void validateAllUsersInTravel_shouldReturnOnNullSet() {
        long travelId = 1;

        travelMemberService.validateAllUsersInTravel(travelId, null);

        verifyNoInteractions(travelMemberRepository);
    }

    @Test
    void validateAllUsersInTravel_shouldThrowWhenUserMissing() {

        long travelId = 1, userId = 2;

        when(travelMemberRepository.existsByTravelIdAndUserIdAndStatus(travelId, userId, MemberStatus.ACCEPTED))
                .thenReturn(false);

        assertThatThrownBy(() -> travelMemberService.validateAllUsersInTravel(travelId, Set.of(userId)))
                .isInstanceOf(UserNotFoundInTravelException.class);
    }

    @Test
    void validateAllUsersInTravel_shouldPassWhenAllExist() {

        long travelId = 1, userId1 = 2, userId2 = 3;

        when(travelMemberRepository.existsByTravelIdAndUserIdAndStatus(travelId, userId1, MemberStatus.ACCEPTED))
                .thenReturn(true);
        when(travelMemberRepository.existsByTravelIdAndUserIdAndStatus(travelId, userId2, MemberStatus.ACCEPTED))
                .thenReturn(true);

        travelMemberService.validateAllUsersInTravel(travelId, Set.of(userId1, userId2));

        verify(travelMemberRepository).existsByTravelIdAndUserIdAndStatus(travelId, userId1, MemberStatus.ACCEPTED);
        verify(travelMemberRepository).existsByTravelIdAndUserIdAndStatus(travelId, userId2, MemberStatus.ACCEPTED);
    }

    @Test
    void findUserInTravel_shouldReturnUser() {

        long travelId = 1, userId = 2, travelMemberId = 3;
        TravelMember tm = TestDataFactory.travelMember(travelMemberId, userId, MemberStatus.ACCEPTED);

        when(travelMemberRepository.findByUserIdAndTravelIdAndStatus(userId, travelId, MemberStatus.ACCEPTED))
                .thenReturn(Optional.of(tm));

        User result = travelMemberService.findUserInTravel(userId, travelId);

        assertThat(result).isEqualTo(tm.getUser());
    }

    @Test
    void findUserInTravel_shouldThrowWhenNotFound() {
        long travelId = 1, userId = 2;

        when(travelMemberRepository.findByUserIdAndTravelIdAndStatus(userId, travelId, MemberStatus.ACCEPTED))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> travelMemberService.findUserInTravel(userId, travelId))
                .isInstanceOf(UserNotFoundInTravelException.class);
    }

    @Test
    void findMemberInTravel_shouldReturnMember() {

        long travelId = 1, userId = 2, travelMemberId = 3;
        TravelMember tm = TestDataFactory.travelMember(travelMemberId, userId);

        when(travelMemberRepository.findByUserIdAndTravelId(userId, travelId)).thenReturn(Optional.of(tm));

        TravelMember result = travelMemberService.findMemberInTravel(userId, travelId);

        assertThat(result).isEqualTo(tm);
    }

    @Test
    void findMemberInTravel_shouldThrowWhenNotFound() {

        long travelId = 1, userId = 2;

        when(travelMemberRepository.findByUserIdAndTravelId(userId, travelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> travelMemberService.findMemberInTravel(userId, travelId))
                .isInstanceOf(UserNotFoundInTravelException.class);
    }

    @Test
    void findInviteInTravelForUser_shouldReturnInvites() {

        long userId = 2, travelMemberId = 3;
        TravelMember member = TestDataFactory.travelMemberInvite(travelMemberId, userId);

        when(travelMemberRepository.findAllByUserIdAndStatus(userId, MemberStatus.INVITED))
                .thenReturn(List.of(member));

        List<TravelMember> result = travelMemberService.findInviteInTravelForUser(userId);

        assertThat(result).containsExactly(member);
    }
}
