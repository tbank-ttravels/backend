package com.tbank.ttravels_backend.service.security;


import com.tbank.ttravels_backend.config.JwtProperties;
import com.tbank.ttravels_backend.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @Mock
    private JwtProperties properties;

    @InjectMocks
    private JwtService jwtService;

    @Test
    void generateAndParseAccessToken_success() {

        // === Given ===
        long userId = 1;
        String phone = "+70001112233";
        String issuer = "issuer";
        String role = "USER";
        Map<String, Object> extraClaims = Collections.singletonMap("role", role);


        // === Mocking ===
        doReturn(issuer).when(properties).getIssuer();
        doReturn(Duration.ofMinutes(15)).when(properties).getAccessTtl();
        doReturn("access-secret-key-123456789012345678901234567890").when(properties).getAccessSecret();


        // === When ===
        String token = jwtService.generateAccessToken(userId, phone, extraClaims);
        Claims claims = jwtService.parseAccess(token);


        // === Then ===
        assertAll(
                () -> assertThat(token).isNotNull(),
                () -> assertThat(claims.getSubject()).isEqualTo(String.valueOf(userId)),
                () -> assertThat(claims.get("phone")).isEqualTo(phone),
                () -> assertThat(claims.get("role")).isEqualTo(role),
                () -> assertThat(claims.getIssuer()).isEqualTo(issuer)
        );


        // === Verify ===
        verify(properties).getIssuer();
        verify(properties).getAccessTtl();
    }


    @Test
    void generateAndParseRefreshToken_success() {

        // === Given ===
        Long refreshTokenId = 99L;
        Long userId = 42L;
        String issuer = "issuer";


        // === Mocking ===
        doReturn(issuer).when(properties).getIssuer();
        doReturn(Duration.ofDays(30)).when(properties).getRefreshTtl();
        doReturn("refresh-secret-key-123456789012345678901234567890").when(properties).getRefreshSecret();


        // === When ===
        String token = jwtService.generateRefreshToken(refreshTokenId, userId);
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(properties.getRefreshSecret().getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();


        // === Then ===
        assertAll(
                () -> assertThat(token).isNotNull(),
                () -> assertThat(claims.getSubject()).isEqualTo(String.valueOf(userId)),
                () -> assertThat(claims.getId()).isEqualTo(String.valueOf(refreshTokenId)),
                () -> assertThat(claims.getIssuer()).isEqualTo(issuer)
        );


        // === Verify ===
        verify(properties).getIssuer();
        verify(properties).getRefreshTtl();
    }
}
