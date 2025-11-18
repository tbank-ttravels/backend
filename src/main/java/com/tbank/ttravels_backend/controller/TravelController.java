package com.tbank.ttravels_backend.controller;

import com.tbank.ttravels_backend.dto.travel.*;
import com.tbank.ttravels_backend.security.UserPrincipal;
import com.tbank.ttravels_backend.service.TravelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/travels")
@RequiredArgsConstructor
public class TravelController {
    private final TravelService travelService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public TravelResponse create(@Valid @RequestBody CreateTravelRequest request,
                                 @AuthenticationPrincipal UserPrincipal principal) {
        return travelService.createTravel(request, principal.getId());
    }

    @GetMapping("/my")
    @ResponseStatus(HttpStatus.OK)
    public MyTravelsResponse getMyTravels(@AuthenticationPrincipal UserPrincipal principal) {
        return travelService.getMyTravels(principal.getId());
    }

    @GetMapping("/{travelId}")
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id)")
    @ResponseStatus(HttpStatus.OK)
    public TravelResponse getTravel(@PathVariable Long travelId,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        return travelService.getTravel(travelId);
    }

    @PatchMapping("/{travelId}")
    @PreAuthorize("@travelSecurity.isOwner(#travelId, principal.id)")
    @ResponseStatus(HttpStatus.OK)
    public TravelResponse editTravel(@PathVariable Long travelId,
                                     @Valid @RequestBody EditTravelRequest request,
                                     @AuthenticationPrincipal UserPrincipal principal) {
        return travelService.editTravel(travelId, request);
    }

    @PostMapping("/{travelId}/close")
    @PreAuthorize("@travelSecurity.isOwner(#travelId, principal.id)")
    @ResponseStatus(HttpStatus.OK)
    public void closeTravel(@PathVariable Long travelId,
                            @AuthenticationPrincipal UserPrincipal principal) {
        travelService.closeTravel(travelId);
    }

    @PostMapping("/{travelId}/reopen")
    @PreAuthorize("@travelSecurity.isOwner(#travelId, principal.id)")
    @ResponseStatus(HttpStatus.OK)
    public void reopenTravel(@PathVariable Long travelId,
                             @AuthenticationPrincipal UserPrincipal principal) {
        travelService.reopenTravel(travelId);
    }

    @DeleteMapping("/{travelId}")
    @PreAuthorize("@travelSecurity.isOwner(#travelId, principal.id)")
    @ResponseStatus(HttpStatus.OK)
    public void deleteTravel(@PathVariable Long travelId,
                             @AuthenticationPrincipal UserPrincipal principal) {
        travelService.deleteTravel(travelId);
    }

    @PostMapping("/{travelId}/invite")
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id)")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void inviteMember(@PathVariable Long travelId,
                             @Valid @RequestBody InviteRequest request,
                             @AuthenticationPrincipal UserPrincipal principal) {
        travelService.inviteMembers(travelId, request.getPhones());
    }

    @GetMapping("/invites")
    @ResponseStatus(HttpStatus.OK)
    public InvitesResponse getInvites(@AuthenticationPrincipal UserPrincipal principal) {
        return travelService.getInvites(principal.getId());
    }

    @PostMapping("/invites/{travelId}/respond")
    @PreAuthorize("@travelSecurity.isInvited(#travelId, principal.id)")
    @ResponseStatus(HttpStatus.OK)
    public void respondToInvite(@PathVariable Long travelId, @RequestParam boolean accept,
                                @AuthenticationPrincipal UserPrincipal principal) {
        travelService.respondToInvite(travelId, principal.getId(), accept);
    }

    @GetMapping("/{travelId}/members")
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id)")
    @ResponseStatus(HttpStatus.OK)
    public TravelMembersResponse getTravelMembers(@PathVariable Long travelId,
                                                  @AuthenticationPrincipal UserPrincipal principal) {
        return travelService.getTravelMembers(travelId);
    }

    @DeleteMapping("/{travelId}/kick/{userId}")
    @PreAuthorize("@travelSecurity.isOwner(#travelId, principal.id)")
    @ResponseStatus(HttpStatus.OK)
    public void kickMember(@PathVariable Long travelId,
                           @PathVariable Long userId,
                           @AuthenticationPrincipal UserPrincipal principal) {
        travelService.kickMember(travelId, userId);
    }

    @PostMapping("/{travelId}/leave")
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id)")
    @ResponseStatus(HttpStatus.OK)
    public void leaveTravel(@PathVariable Long travelId,
                            @AuthenticationPrincipal UserPrincipal principal) {
        travelService.leaveTravel(travelId, principal.getId());
    }

}
