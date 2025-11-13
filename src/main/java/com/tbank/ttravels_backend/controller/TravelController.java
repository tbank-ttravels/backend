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
@RequestMapping("/travels")
@RequiredArgsConstructor
public class TravelController {
    private final TravelService travelService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateTravelResponse create(@Valid @RequestBody CreateTravelRequest request,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        return travelService.createTravel(request, principal.getId());
    }

    @GetMapping("/travels")
    @ResponseStatus(HttpStatus.OK)
    public MyTravelsResponse getMyTravels(@AuthenticationPrincipal UserPrincipal principal) {
        return travelService.getMyTravels(principal.getId());
    }

    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id)")
    @PostMapping("/{travelId}/invite")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void inviteMember(@PathVariable Long travelId,
                             @Valid @RequestBody InviteRequest request) {
        travelService.inviteMember(travelId, request.getPhone());
    }

    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id)")
    @GetMapping("/{travelId}/members")
    @ResponseStatus(HttpStatus.OK)
    public TravelMembersResponse getTravelMembers(@PathVariable Long travelId) {
        return travelService.getTravelMembers(travelId);
    }
}
