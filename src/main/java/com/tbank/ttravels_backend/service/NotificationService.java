package com.tbank.ttravels_backend.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.MessagingErrorCode;
import com.tbank.ttravels_backend.dto.notification.PushNotificationRequest;
import com.tbank.ttravels_backend.dto.notification.PushNotificationResponse;
import com.tbank.ttravels_backend.entity.DeviceToken;
import com.tbank.ttravels_backend.enums.Platform;
import com.tbank.ttravels_backend.repository.DeviceTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.List;


@Service
@Slf4j
public class NotificationService {

    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    public PushNotificationResponse sendToUser(Long userId, PushNotificationRequest request) {
        List<DeviceToken> tokens = deviceTokenRepository.findByUserIdAndIsActiveTrue(userId);

        if (tokens.isEmpty()) {
            return PushNotificationResponse.builder()
                    .success(false)
                    .message("Нет активных токенов")
                    .build();
        }

        int successCount = 0;
    String lastMessageId = null;


     for (DeviceToken deviceToken : tokens) {
        try {
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(request.getTitle())
                            .setBody(request.getBody())
                            .build())
                    .putAllData(request.getData() != null ? request.getData() : Map.of())
                    .setToken(deviceToken.getToken())
                    .build();

            lastMessageId = FirebaseMessaging.getInstance().send(message);
            successCount++;

        } catch (FirebaseMessagingException e) {
            log.warn("Ошибка отправки на токен {}: {}",
                    deviceToken.getToken(),
                    e.getMessagingErrorCode());

            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED
                    || e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                deviceTokenRepository.deactivateByToken(deviceToken.getToken());
            }
        }
    }
        if (successCount == 0) {
        return PushNotificationResponse.builder()
                .success(false)
                .message("Не удалось доставить уведомление")
                .build();
    }
    
        return PushNotificationResponse.builder()
                .success(true)
                .messageId(lastMessageId)
                .message("Уведомление отправлено на " + successCount +" устройств")
                .build();
    }

    public PushNotificationResponse sendToTopic(String topic, PushNotificationRequest request) {
        try {
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(request.getTitle())
                            .setBody(request.getBody())
                            .build())
                    .putAllData(request.getData() != null ? request.getData() : new HashMap<>())
                    .setTopic(topic)
                    .build();

            String messageId = FirebaseMessaging.getInstance().send(message);

            log.info("Уведомление отправлено в топик: {}. MessageId: {}", topic, messageId);

            return PushNotificationResponse.builder()
                    .success(true)
                    .messageId(messageId)
                    .message("Уведомление отправлено в топик")
                    .build();

        } catch (FirebaseMessagingException e) {
            log.error("Ошибка при отправке уведомления в топик: {}", topic, e);
            return PushNotificationResponse.builder()
                    .success(false)
                    .message("Ошибка: " + e.getMessage())
                    .build();
        }
    }

    public void registerDeviceToken(Long userId, String token, Platform platform){
            DeviceToken deviceToken = deviceTokenRepository.findByToken(token)
                .map(existing -> {
                    existing.setUserId(userId);
                    existing.setPlatform(platform);
                    existing.setIsActive(true);
                    return existing;
                })
                .orElseGet(() -> DeviceToken.builder()
                        .userId(userId)
                        .token(token)
                        .platform(platform)
                        .isActive(true)
                        .build()
                );
            deviceTokenRepository.save(deviceToken);
            log.info("Токен зарегистрирован для пользователя: {} | Платформа: {}", userId, platform);
    }

    public void unregisterDeviceToken(String token) {
           int updated = deviceTokenRepository.deactivateByToken(token);
        if (updated == 0) {
        throw new IllegalStateException("Токен не найден или деактивирован");
    }
    log.info("Токен деактивирован: {}", token);
    }
}
