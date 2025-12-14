package com.tbank.ttravels_backend.controller;

import com.tbank.ttravels_backend.enums.Platform;
import com.tbank.ttravels_backend.service.NotificationService;
import com.tbank.ttravels_backend.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@Slf4j
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/register-token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> registerToken(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam String token,
            @RequestParam(defaultValue = "ANDROID") Platform platform) {

        notificationService.registerDeviceToken(user.getId(), token, platform);
        log.info("Токен зарегистрирован для пользователя {}", user.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unregister-token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unregisterToken(@RequestParam String token) {
        notificationService.unregisterDeviceToken(token);
        log.info("Токен удалён");
        return ResponseEntity.ok().build();
    }
}
