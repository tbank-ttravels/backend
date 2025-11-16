package com.tbank.ttravels_backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.security.jwt")
@Getter @Setter
public class JwtProperties {
    private String issuer;
    private String accessSecret;
    private Duration accessTtl;
    private String refreshSecret;
    private Duration refreshTtl;
}
