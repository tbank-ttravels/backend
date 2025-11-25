package com.tbank.ttravels_backend.controller;

import com.tbank.ttravels_backend.dto.travel.member.InviteRequest;
import com.tbank.ttravels_backend.dto.travel.member.TravelMembersResponse;
import com.tbank.ttravels_backend.security.UserPrincipal;
import com.tbank.ttravels_backend.service.TravelMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/travels/{travelId}/members")
@RequiredArgsConstructor
public class TravelMemberController {
    private final TravelMemberService travelMemberService;

    @PostMapping("/invite")
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id)")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void inviteMember(@PathVariable Long travelId,
                             @Valid @RequestBody InviteRequest request,
                             @AuthenticationPrincipal UserPrincipal principal) {
        travelMemberService.inviteMembers(travelId, request.getPhones());
    }

    @GetMapping()
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id)")
    @ResponseStatus(HttpStatus.OK)
    public TravelMembersResponse getTravelMembers(@PathVariable Long travelId,
                                                  @AuthenticationPrincipal UserPrincipal principal) {
        return travelMemberService.getTravelMembers(travelId);
    }

    @DeleteMapping("/kick/{userId}")
    @PreAuthorize("@travelSecurity.isOwner(#travelId, principal.id)")
    @ResponseStatus(HttpStatus.OK)
    public void kickMember(@PathVariable Long travelId,
                           @PathVariable Long userId,
                           @AuthenticationPrincipal UserPrincipal principal) {
        travelMemberService.kickMember(travelId, userId);
    }

    @PostMapping("/leave")
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id)")
    @ResponseStatus(HttpStatus.OK)
    public void leaveTravel(@PathVariable Long travelId,
                            @AuthenticationPrincipal UserPrincipal principal) {
        travelMemberService.leaveTravel(travelId, principal.getId());
    }
}