package com.tbank.ttravels_backend.security;

import com.tbank.ttravels_backend.dto.auth.AccountResponse;
import com.tbank.ttravels_backend.dto.auth.AuthResponse;
import com.tbank.ttravels_backend.dto.auth.ChangePasswordRequest;
import com.tbank.ttravels_backend.dto.auth.RefreshOrLogoutRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/password/change")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal UserPrincipal principal,
                               @Valid @RequestBody ChangePasswordRequest request) {
        accountService.changePassword(principal.getId(), request);
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
