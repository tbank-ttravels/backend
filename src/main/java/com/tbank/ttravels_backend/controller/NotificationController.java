package com.tbank.ttravels_backend.controller;

import com.tbank.ttravels_backend.dto.notification.PushNotificationRequest;
import com.tbank.ttravels_backend.dto.notification.PushNotificationResponse;
import com.tbank.ttravels_backend.enums.Platform;
import com.tbank.ttravels_backend.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@Slf4j
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/register-token")
    public ResponseEntity<?> registerToken(
            @RequestParam Long userId,
            @RequestParam String token,
            @RequestParam(defaultValue = "ANDROID") Platform platform) {

        notificationService.registerDeviceToken(userId, token, platform);
        log.info("Токен зарегистрирован для пользователя {}", userId);
        return ResponseEntity.ok("Токен успешно зарегистрирован");
    }

    @PostMapping("/unregister-token")
    public ResponseEntity<?> unregisterToken(@RequestParam String token) {
        notificationService.unregisterDeviceToken(token);
        log.info("Токен удалён");
        return ResponseEntity.ok("Токен успешно удалён");
    }
}
