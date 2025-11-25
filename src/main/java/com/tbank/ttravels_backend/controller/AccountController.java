package com.tbank.ttravels_backend.controller;

import com.tbank.ttravels_backend.dto.auth.AccountResponse;
import com.tbank.ttravels_backend.dto.auth.AuthResponse;
import com.tbank.ttravels_backend.dto.auth.ChangePasswordRequest;
import com.tbank.ttravels_backend.dto.auth.RefreshOrLogoutRequest;
import com.tbank.ttravels_backend.dto.travel.member.InvitesResponse;
import com.tbank.ttravels_backend.security.UserPrincipal;
import com.tbank.ttravels_backend.service.AccountService;
import com.tbank.ttravels_backend.service.TravelMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    private final TravelMemberService travelMemberService;

    @PostMapping("/password/change")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal UserPrincipal principal,
                               @Valid @RequestBody ChangePasswordRequest request) {
        accountService.changePassword(principal.getId(), request);
    }

    @GetMapping("/invites")
    @ResponseStatus(HttpStatus.OK)
    public InvitesResponse getInvites(@AuthenticationPrincipal UserPrincipal principal) {
        return travelMemberService.getInvites(principal.getId());
    }

    @PostMapping("/invites/respond/{inviteId}")
    @PreAuthorize("@travelSecurity.isInvited(#inviteId, principal.id)")
    @ResponseStatus(HttpStatus.OK)
    public void respondToInvite(@PathVariable Long inviteId, @RequestParam boolean accept,
                                @AuthenticationPrincipal UserPrincipal principal) {
        travelMemberService.respondToInvite(inviteId, principal.getId(), accept);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal UserPrincipal principal,
                       @Valid @RequestBody RefreshOrLogoutRequest request) {
        accountService.logout(principal.getId(), request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshOrLogoutRequest request) {
        return accountService.refresh(request);
    }

    @GetMapping("/me")
    public AccountResponse getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        return accountService.getCurrentUser(principal.getId());
    }
}
