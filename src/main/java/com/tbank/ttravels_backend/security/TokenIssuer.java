package com.tbank.ttravels_backend.security;

import com.tbank.ttravels_backend.config.JwtProperties;
import com.tbank.ttravels_backend.dto.auth.AuthResponse;
import com.tbank.ttravels_backend.entity.RefreshToken;
import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.exception.RefreshTokenExpiredException;
import com.tbank.ttravels_backend.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TokenIssuer {

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHashService tokenHashService;

    @Transactional
    public AuthResponse issue(User user) {
        RefreshToken refreshToken = refreshTokenRepository.save(
                RefreshToken.builder()
                        .user(user)
                        .tokenHash("PENDING")
                        .expiresAt(now().plus(jwtProperties.getRefreshTtl()))
                        .revoked(false)
                        .build()
        );

        String refreshJwt = jwtService.generateRefreshToken(refreshToken.getId(), user.getId());
        refreshToken.setTokenHash(tokenHashService.hash(refreshJwt));
        refreshTokenRepository.save(refreshToken);

        String accessJwt = jwtService.generateAccessToken(user.getId(), user.getPhone(), Map.of());
        return response(accessJwt, refreshJwt);
    }

    @Transactional
    public AuthResponse rotate(RefreshToken storedToken) {
        if (storedToken.isRevoked() || storedToken.getExpiresAt().isBefore(now())) {
            throw new RefreshTokenExpiredException("Refresh token истёк");
        }

        RefreshToken newToken = refreshTokenRepository.save(
                RefreshToken.builder()
                        .user(storedToken.getUser())
                        .tokenHash("PENDING")
                        .expiresAt(now().plus(jwtProperties.getRefreshTtl()))
                        .revoked(false)
                        .build()
        );

        String refreshJwt = jwtService.generateRefreshToken(newToken.getId(), storedToken.getUser().getId());
        newToken.setTokenHash(tokenHashService.hash(refreshJwt));
        refreshTokenRepository.save(newToken);

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        String accessJwt = jwtService.generateAccessToken(
                storedToken.getUser().getId(),
                storedToken.getUser().getPhone(),
                Map.of()
        );

        return response(accessJwt, refreshJwt);
    }

    private AuthResponse response(String access, String refresh) {
        return new AuthResponse(
                access,
                jwtProperties.getAccessTtl().toSeconds(),
                refresh,
                jwtProperties.getRefreshTtl().toSeconds()
        );
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
