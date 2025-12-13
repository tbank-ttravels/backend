package com.tbank.ttravels_backend.CopyTravel;

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
import com.tbank.ttravels_backend.service.TravelMemberService;
import com.tbank.ttravels_backend.service.TravelService;
import org.junit.jupiter.api.BeforeEach;
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

    private Travel travel;
    private User user;
    private TravelMember member;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(5L)
                .phone("+78005550101")
                .name("Пётр")
                .surname("Петров")
                .build();

        travel = Travel.builder()
                .id(42L)
                .name("Поездка мечты")
                .owner(user)
                .travelMembers(Set.of())
                .build();

        member = TravelMember.builder()
                .id(100L)
                .user(user)
                .travel(travel)
                .status(MemberStatus.ACCEPTED)
                .role(MemberRole.MEMBER)
                .build();
    }

    @Test
    void inviteMembers_shouldReturnWhenPhonesEmpty() {
        travelMemberService.inviteMembers(42L, List.of());

        verifyNoInteractions(travelService, accountService, travelMemberRepository);
    }

    @Test
    void inviteMembers_shouldInviteDistinctTrimmedPhones() {
        String phone1 = " +78001112233 ";
        String phone2 = "+78001112233";
        String phone3 = "+79009998877";
        User invited1 = User.builder().id(10L).phone("+78001112233").name("Алексей").build();
        User invited2 = User.builder().id(11L).phone("+79009998877").name("Сергей").build();

        when(travelService.findTravel(42L)).thenReturn(travel);
        when(accountService.findUserByPhone("+78001112233")).thenReturn(invited1);
        when(accountService.findUserByPhone("+79009998877")).thenReturn(invited2);
        when(travelMemberRepository.existsByTravelIdAndUserId(42L, 10L)).thenReturn(false);
        when(travelMemberRepository.existsByTravelIdAndUserId(42L, 11L)).thenReturn(false);

        travelMemberService.inviteMembers(42L, List.of(phone1, phone2, phone3));

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
        assertThat(created).extracting(m -> m.getUser().getId()).containsExactlyInAnyOrder(10L, 11L);
    }

    @Test
    void inviteMembers_shouldThrowWhenConflict() {
        String phone = "+78001112233";
        User invited = User.builder().id(10L).phone(phone).name("Алексей").build();
        when(travelService.findTravel(42L)).thenReturn(travel);
        when(accountService.findUserByPhone(phone)).thenReturn(invited);
        when(travelMemberRepository.existsByTravelIdAndUserId(42L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> travelMemberService.inviteMembers(42L, List.of(phone)))
                .isInstanceOf(ConflictStateException.class);

        verify(travelService, never()).addTravelMember(any(), any());
        verify(travelService, never()).saveTravel(any());
    }

    @Test
    void getMyTravels_shouldMapResponse() {
        TravelMember tm = TravelMember.builder().id(1L).travel(travel).build();
        when(travelMemberRepository.findAllByUserIdAndStatus(5L, MemberStatus.ACCEPTED))
                .thenReturn(List.of(tm));
        MyTravelsResponse expected = new MyTravelsResponse(List.of());
        when(travelMapper.toMyTravelsResponse(List.of(travel))).thenReturn(expected);

        MyTravelsResponse response = travelMemberService.getMyTravels(5L);

        assertThat(response).isEqualTo(expected);
        verify(travelMemberRepository).findAllByUserIdAndStatus(5L, MemberStatus.ACCEPTED);
        verify(travelMapper).toMyTravelsResponse(List.of(travel));
    }

    @Test
    void getInvites_shouldMapResponse() {
        TravelMember invite = TravelMember.builder().id(2L).build();
        InvitesResponse expected = new InvitesResponse(List.of());
        when(travelMemberRepository.findAllByUserIdAndStatus(5L, MemberStatus.INVITED))
                .thenReturn(List.of(invite));
        when(travelMemberMapper.toInvitesResponse(List.of(invite))).thenReturn(expected);

        InvitesResponse response = travelMemberService.getInvites(5L);

        assertThat(response).isEqualTo(expected);
        verify(travelMemberRepository).findAllByUserIdAndStatus(5L, MemberStatus.INVITED);
        verify(travelMemberMapper).toInvitesResponse(List.of(invite));
    }

    @Test
    void respondToInvite_shouldAccept() {
        TravelMember invite = TravelMember.builder()
                .id(7L)
                .user(user)
                .status(MemberStatus.INVITED)
                .build();
        when(travelMemberRepository.findByIdAndUserIdAndStatus(7L, 5L, MemberStatus.INVITED))
                .thenReturn(Optional.of(invite));
        when(travelMemberRepository.save(any(TravelMember.class))).thenAnswer(inv -> inv.getArgument(0));

        travelMemberService.respondToInvite(7L, 5L, true);

        assertThat(invite.getStatus()).isEqualTo(MemberStatus.ACCEPTED);
        verify(travelMemberRepository).save(invite);
    }

    @Test
    void respondToInvite_shouldReject() {
        TravelMember invite = TravelMember.builder()
                .id(7L)
                .user(user)
                .status(MemberStatus.INVITED)
                .build();
        when(travelMemberRepository.findByIdAndUserIdAndStatus(7L, 5L, MemberStatus.INVITED))
                .thenReturn(Optional.of(invite));

        travelMemberService.respondToInvite(7L, 5L, false);

        assertThat(invite.getStatus()).isEqualTo(MemberStatus.REJECTED);
        verify(travelMemberRepository).save(invite);
    }

    @Test
    void respondToInvite_shouldThrowWhenNotFound() {
        when(travelMemberRepository.findByIdAndUserIdAndStatus(7L, 5L, MemberStatus.INVITED))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> travelMemberService.respondToInvite(7L, 5L, true))
                .isInstanceOf(InviteNotFoundException.class);

        verify(travelMemberRepository, never()).save(any());
    }

    @Test
    void getTravelMembers_shouldReturnMapped() {
        TravelMember tm = TravelMember.builder().id(3L).travel(travel).user(user).build();
        TravelMembersResponse expected = new TravelMembersResponse(List.of());
        when(travelMemberRepository.findByTravelId(42L)).thenReturn(List.of(tm));
        when(travelMemberMapper.toMembersResponse(List.of(tm))).thenReturn(expected);

        TravelMembersResponse response = travelMemberService.getTravelMembers(42L);

        assertThat(response).isEqualTo(expected);
        verify(travelMemberRepository).findByTravelId(42L);
        verify(travelMemberMapper).toMembersResponse(List.of(tm));
    }

    @Test
    void kickMember_shouldMarkMemberAsLeft() {
        TravelMember toKick = TravelMember.builder()
                .id(4L)
                .travel(travel)
                .user(user)
                .role(MemberRole.MEMBER)
                .status(MemberStatus.ACCEPTED)
                .build();
        when(travelMemberRepository.findByUserIdAndTravelId(5L, 42L)).thenReturn(Optional.of(toKick));

        travelMemberService.kickMember(42L, 5L);

        assertThat(toKick.getStatus()).isEqualTo(MemberStatus.LEAVE);
        verify(travelMemberRepository).findByUserIdAndTravelId(5L, 42L);
        verifyNoInteractions(travelService);
    }

    @Test
    void kickMember_shouldThrowWhenOwner() {
        TravelMember ownerMember = TravelMember.builder()
                .id(4L)
                .travel(travel)
                .user(user)
                .role(MemberRole.OWNER)
                .status(MemberStatus.ACCEPTED)
                .build();
        when(travelMemberRepository.findByUserIdAndTravelId(5L, 42L)).thenReturn(Optional.of(ownerMember));

        assertThatThrownBy(() -> travelMemberService.kickMember(42L, 5L))
                .isInstanceOf(OwnerRemovalNotAllowedException.class);

        assertThat(ownerMember.getStatus()).isEqualTo(MemberStatus.ACCEPTED);
        verify(travelMemberRepository).findByUserIdAndTravelId(5L, 42L);
        verifyNoInteractions(travelService);
    }

    @Test
    void leaveTravel_shouldMarkMemberAsLeft() {
        TravelMember toLeave = TravelMember.builder()
                .id(6L)
                .travel(travel)
                .user(user)
                .role(MemberRole.MEMBER)
                .status(MemberStatus.ACCEPTED)
                .build();
        when(travelMemberRepository.findByUserIdAndTravelId(5L, 42L)).thenReturn(Optional.of(toLeave));

        travelMemberService.leaveTravel(42L, 5L);

        assertThat(toLeave.getStatus()).isEqualTo(MemberStatus.LEAVE);
        verify(travelMemberRepository).findByUserIdAndTravelId(5L, 42L);
        verifyNoInteractions(travelService);
    }

    @Test
    void leaveTravel_shouldThrowWhenOwner() {
        TravelMember ownerMember = TravelMember.builder()
                .id(6L)
                .travel(travel)
                .user(user)
                .role(MemberRole.OWNER)
                .status(MemberStatus.ACCEPTED)
                .build();
        when(travelMemberRepository.findByUserIdAndTravelId(5L, 42L)).thenReturn(Optional.of(ownerMember));

        assertThatThrownBy(() -> travelMemberService.leaveTravel(42L, 5L))
                .isInstanceOf(OwnerRemovalNotAllowedException.class);

        assertThat(ownerMember.getStatus()).isEqualTo(MemberStatus.ACCEPTED);
        verify(travelMemberRepository).findByUserIdAndTravelId(5L, 42L);
        verifyNoInteractions(travelService);
    }

    @Test
    void validateAllUsersInTravel_shouldReturnOnNullSet() {
        travelMemberService.validateAllUsersInTravel(42L, null);

        verifyNoInteractions(travelMemberRepository);
    }

    @Test
    void validateAllUsersInTravel_shouldThrowWhenUserMissing() {
        when(travelMemberRepository.existsByTravelIdAndUserIdAndStatus(42L, 7L, MemberStatus.ACCEPTED))
                .thenReturn(false);

        assertThatThrownBy(() -> travelMemberService.validateAllUsersInTravel(42L, Set.of(7L)))
                .isInstanceOf(UserNotFoundInTravelException.class);
    }

    @Test
    void validateAllUsersInTravel_shouldPassWhenAllExist() {
        when(travelMemberRepository.existsByTravelIdAndUserIdAndStatus(42L, 7L, MemberStatus.ACCEPTED))
                .thenReturn(true);
        when(travelMemberRepository.existsByTravelIdAndUserIdAndStatus(42L,  8L, MemberStatus.ACCEPTED))
                .thenReturn(true);

        travelMemberService.validateAllUsersInTravel(42L, Set.of(7L, 8L));

        verify(travelMemberRepository).existsByTravelIdAndUserIdAndStatus(42L, 7L, MemberStatus.ACCEPTED);
        verify(travelMemberRepository).existsByTravelIdAndUserIdAndStatus(42L,  8L, MemberStatus.ACCEPTED);
    }

    @Test
    void findUserInTravel_shouldReturnUser() {
        TravelMember tm = TravelMember.builder().user(user).build();
        when(travelMemberRepository.findByUserIdAndTravelIdAndStatus(5L, 42L, MemberStatus.ACCEPTED))
                .thenReturn(Optional.of(tm));

        User result = travelMemberService.findUserInTravel(5L, 42L);

        assertThat(result).isEqualTo(user);
    }

    @Test
    void findUserInTravel_shouldThrowWhenNotFound() {
        when(travelMemberRepository.findByUserIdAndTravelIdAndStatus(5L, 42L, MemberStatus.ACCEPTED))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> travelMemberService.findUserInTravel(5L, 42L))
                .isInstanceOf(UserNotFoundInTravelException.class);
    }

    @Test
    void findMemberInTravel_shouldReturnMember() {
        when(travelMemberRepository.findByUserIdAndTravelId(5L, 42L)).thenReturn(Optional.of(member));

        TravelMember result = travelMemberService.findMemberInTravel(5L, 42L);

        assertThat(result).isEqualTo(member);
    }

    @Test
    void findMemberInTravel_shouldThrowWhenNotFound() {
        when(travelMemberRepository.findByUserIdAndTravelId(5L, 42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> travelMemberService.findMemberInTravel(5L, 42L))
                .isInstanceOf(UserNotFoundInTravelException.class);
    }

    @Test
    void findInviteInTravelForUser_shouldReturnInvites() {
        when(travelMemberRepository.findAllByUserIdAndStatus(5L, MemberStatus.INVITED))
                .thenReturn(List.of(member));

        List<TravelMember> result = travelMemberService.findInviteInTravelForUser(5L);

        assertThat(result).containsExactly(member);
    }
}
