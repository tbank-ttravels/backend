package com.tbank.ttravels_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Slf4j
@Configuration
public class NotificationConfig {

    @Value("${firebase.service-account-path:}")
    private String serviceAccountPath;

    @Bean
    @ConditionalOnProperty(name = "firebase.service-account-path")
    public FirebaseApp firebaseApp() {
        log.info("Попытка инициализировать Firebase, используя путь service-account-path='{}'", serviceAccountPath);

        try {
            if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
                throw new IllegalStateException("Параметр firebase.service-account-path пустой или не указан");
            }

            ClassPathResource resource = new ClassPathResource(serviceAccountPath);
            if (!resource.exists()) {
                throw new IllegalStateException("Файл service account Firebase не найден в classpath по указанному пути: " + serviceAccountPath);
            }

            try (var is = resource.getInputStream()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(is))
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                    log.info("Firebase инициализирован");
                } else {
                    log.info("Firebase уже был инициализирован ранее (используется существующий экземпляр)");
                }
            }

            return FirebaseApp.getInstance();

        } catch (IOException e) {
            log.error("Не удалось прочитать файл service account Firebase (путь ={}): {}", serviceAccountPath, e.getMessage(), e);
            throw new IllegalStateException("Не удалось прочитать файл service account Firebase: " + serviceAccountPath, e);
        } catch (IllegalStateException e) {
            log.error("Ошибка инициализации Firebase: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Непредвиденная ошибка во время инициализации Firebase: {}", e.getMessage(), e);
            throw new IllegalStateException("Непредвиденная ошибка при инициализации Firebase", e);
        }
    }
}
