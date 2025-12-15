package com.tbank.ttravels_backend.service.security;

import com.tbank.ttravels_backend.config.JwtProperties;
import com.tbank.ttravels_backend.dto.auth.AuthResponse;
import com.tbank.ttravels_backend.entity.RefreshToken;
import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.exception.RefreshTokenExpiredException;
import com.tbank.ttravels_backend.repository.RefreshTokenRepository;
import com.tbank.ttravels_backend.security.JwtService;
import com.tbank.ttravels_backend.security.TokenHashService;
import com.tbank.ttravels_backend.security.TokenIssuer;
import com.tbank.ttravels_backend.service.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenIssuerTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenHashService tokenHashService;

    @InjectMocks
    private TokenIssuer tokenIssuer;


    @Test
    void issue_createsTokensAndSavesRefreshToken() {

        User user = TestDataFactory.user(1, "+70001112233");
        RefreshToken savedToken = TestDataFactory.refreshToken(1);

        doReturn(Duration.ofDays(30)).when(jwtProperties).getRefreshTtl();
        doReturn(Duration.ofMinutes(15)).when(jwtProperties).getAccessTtl();
        doReturn(savedToken).when(refreshTokenRepository).save(any());
        doReturn("refresh-jwt").when(jwtService).generateRefreshToken(anyLong(), anyLong());
        doReturn("access-jwt").when(jwtService).generateAccessToken(anyLong(), anyString(), any());
        doReturn("hashed-refresh").when(tokenHashService).hash("refresh-jwt");

        AuthResponse response = tokenIssuer.issue(user);

        assertAll(
                () -> assertThat(response.getAccessToken()).isEqualTo("access-jwt"),
                () -> assertThat(response.getRefreshToken()).isEqualTo("refresh-jwt"),
                () -> assertThat(response.getAccessTokenExpiresIn()).isEqualTo(15 * 60),
                () -> assertThat(response.getRefreshTokenExpiresIn()).isEqualTo(30 * 24 * 3600)
        );

        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
        verify(tokenHashService).hash("refresh-jwt");
    }

    @Test
    void rotate_revokesOldTokenAndIssuesNewTokens() {

        User user = TestDataFactory.user(1, "+70001112233");

        RefreshToken oldToken = TestDataFactory.refreshToken(99, user, false,
                OffsetDateTime.now(ZoneOffset.UTC).plusDays(1));

        RefreshToken newToken = TestDataFactory.refreshToken(100, user, false,
                OffsetDateTime.now(ZoneOffset.UTC).plusDays(30));

        doReturn(Duration.ofDays(30)).when(jwtProperties).getRefreshTtl();
        doReturn(Duration.ofMinutes(15)).when(jwtProperties).getAccessTtl();
        doReturn(newToken).when(refreshTokenRepository).save(any());
        doReturn("new-refresh-jwt").when(jwtService).generateRefreshToken(anyLong(), anyLong());
        doReturn("new-access-jwt").when(jwtService).generateAccessToken(anyLong(), anyString(), any());
        doReturn("hashed-new-refresh").when(tokenHashService).hash("new-refresh-jwt");

        AuthResponse response = tokenIssuer.rotate(oldToken);

        assertAll(
                () -> assertThat(response.getAccessToken()).isEqualTo("new-access-jwt"),
                () -> assertThat(response.getRefreshToken()).isEqualTo("new-refresh-jwt"),
                () -> assertThat(oldToken.isRevoked()).isTrue()
        );

        verify(refreshTokenRepository, times(3)).save(any(RefreshToken.class));
        verify(tokenHashService).hash("new-refresh-jwt");
    }

    @Test
    void rotate_throwsException_whenOldTokenExpired() {

        User user = TestDataFactory.user(1, "+70001112233");

        RefreshToken expiredToken = TestDataFactory.refreshToken(1, user, false,
                OffsetDateTime.now(ZoneOffset.UTC).minusSeconds(1));

        assertThrows(RefreshTokenExpiredException.class, () -> tokenIssuer.rotate(expiredToken));
        assertThat(expiredToken.isRevoked()).isFalse();
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void rotate_throwsException_whenOldTokenRevoked() {

        User user = TestDataFactory.user(1, "+70001112233");

        RefreshToken revokedToken = TestDataFactory.refreshToken(1, user, true,
                OffsetDateTime.now(ZoneOffset.UTC).plusDays(1));

        assertThrows(RefreshTokenExpiredException.class, () -> tokenIssuer.rotate(revokedToken));
        verify(refreshTokenRepository, never()).save(any());
    }
}
