package com.tbank.ttravels_backend.controller;

import com.tbank.ttravels_backend.dto.travel.CreateTravelRequest;
import com.tbank.ttravels_backend.dto.travel.EditTravelRequest;
import com.tbank.ttravels_backend.dto.travel.MyTravelsResponse;
import com.tbank.ttravels_backend.dto.travel.TravelResponse;
import com.tbank.ttravels_backend.security.UserPrincipal;
import com.tbank.ttravels_backend.service.TravelMemberService;
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
    private final TravelMemberService travelMemberService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public TravelResponse create(@Valid @RequestBody CreateTravelRequest request,
                                 @AuthenticationPrincipal UserPrincipal principal) {
        return travelService.createTravel(request, principal.getId());
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public MyTravelsResponse getMyTravels(@AuthenticationPrincipal UserPrincipal principal) {
        return travelMemberService.getMyTravels(principal.getId());
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
}
