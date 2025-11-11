package com.tbank.ttravels_backend.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration}")
    private long expirationTime;

    @PostConstruct
    public void validateSecretKey() {
        if (secretKey == null || secretKey.length() < 32) {
            logger.error("JWT secret key must be at least 32 characters long! Current length: {}",
                    secretKey != null ? secretKey.length() : "null");
            throw new IllegalStateException(
                    "JWT secret key must be at least 32 characters long! " +
                            "Update app.jwt.secret in application.properties"
            );
        }
        logger.info("JWT Secret Key validated (length: {})", secretKey.length());
    }

    public String generateToken(Long id, String phone) {
        logger.info("Generating JWT token for id: {}, phone: {}", id, phone);

        String token = Jwts.builder()
                .setSubject(id.toString())
                .claim("phone", phone)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        logger.info("JWT token generated successfully");
        return token;
    }
}