package com.tbank.ttravels_backend.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.tbank.ttravels_backend.dto.notification.PushNotificationRequest;
import com.tbank.ttravels_backend.dto.notification.PushNotificationResponse;
import com.tbank.ttravels_backend.entity.DeviceToken;
import com.tbank.ttravels_backend.enums.Platform;
import com.tbank.ttravels_backend.repository.DeviceTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;


@Service
@Slf4j
public class NotificationService {

    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    public PushNotificationResponse sendToUser(Long userId, PushNotificationRequest request) {
        List<DeviceToken> tokens = deviceTokenRepository.findByUserIdAndIsActiveTrue(userId);

        if (tokens.isEmpty()) {
            log.warn("Нет активных токенов для пользователя: {}", userId);
            return PushNotificationResponse.builder()
                    .success(false)
                    .message("Нет активных токенов для пользователя")
                    .build();
        }

        String messageId = null;
        for (DeviceToken token : tokens) {
            PushNotificationResponse response = sendToToken(token.getToken(), request);
            if (response.getSuccess()) {
                messageId = response.getMessageId();
            }
        }

        return PushNotificationResponse.builder()
                .success(true)
                .messageId(messageId)
                .message("Уведомление отправлено устройствам" + tokens.size())
                .build();
    }

    public PushNotificationResponse sendToToken(String token, PushNotificationRequest request) {
        try {
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(request.getTitle())
                            .setBody(request.getBody())
                            .build())
                    .putAllData(request.getData() != null ? request.getData() : new HashMap<>())
                    .setToken(token)
                    .build();

            String messageId = FirebaseMessaging.getInstance().send(message);

            log.info("Уведомление успешно отправлено. MessageId: {}", messageId);

            return PushNotificationResponse.builder()
                    .success(true)
                    .messageId(messageId)
                    .message("Уведомление успешно отправлено")
                    .build();

        } catch (FirebaseMessagingException e) {
            log.error("Ошибка при отправке уведомления на токен: {}", token, e);
            return PushNotificationResponse.builder()
                    .success(false)
                    .message("Ошибка: " + e.getMessage())
                    .build();
        }
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

    public void registerDeviceToken(Long userId, String token, Platform platform) {
        try {
            DeviceToken deviceToken = DeviceToken.builder()
                    .userId(userId)
                    .token(token)
                    .platform(platform)
                    .isActive(true)
                    .build();

            deviceTokenRepository.save(deviceToken);
            log.info("Токен зарегистрирован для пользователя: {} | Платформа: {}", userId, platform);

        } catch (Exception e) {
            log.error("Ошибка при регистрации токена", e);
        }
    }

    public void unregisterDeviceToken(String token) {
        try {
            deviceTokenRepository.deleteByToken(token);
            log.info("Токен удалён");
        } catch (Exception e) {
            log.error("Ошибка при удалении токена", e);
        }
    }
}
