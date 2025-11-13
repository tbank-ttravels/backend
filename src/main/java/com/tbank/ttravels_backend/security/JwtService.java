package com.tbank.ttravels_backend.security;

import com.tbank.ttravels_backend.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties properties;

    public String generateAccessToken(Long userId,
                                      String phone,
                                      Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuer(properties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.getAccessTtl())))
                .claims(extraClaims)
                .claim("phone", phone)
                .signWith(accessKey())
                .compact();
    }

    public String generateRefreshToken(Long refreshTokenId, Long userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuer(properties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.getRefreshTtl())))
                .id(String.valueOf(refreshTokenId))
                .signWith(refreshKey())
                .compact();
    }

    public Claims parseAccess(String token) {
        return Jwts.parser()
                .verifyWith(accessKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey accessKey() {
        byte[] keyBytes = properties.getAccessSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey refreshKey() {
        byte[] keyBytes = properties.getRefreshSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    }