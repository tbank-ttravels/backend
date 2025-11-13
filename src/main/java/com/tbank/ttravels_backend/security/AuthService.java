package com.tbank.ttravels_backend.security;

import com.tbank.ttravels_backend.config.JwtProperties;
import com.tbank.ttravels_backend.dto.auth.AuthLoginRequest;
import com.tbank.ttravels_backend.dto.auth.AuthRegisterRequest;
import com.tbank.ttravels_backend.dto.auth.AuthResponse;
import com.tbank.ttravels_backend.entity.PasswordCredential;
import com.tbank.ttravels_backend.entity.RefreshToken;
import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.repository.PasswordCredentialRepository;
import com.tbank.ttravels_backend.repository.RefreshTokenRepository;
import com.tbank.ttravels_backend.repository.UserRepository;
import com.tbank.ttravels_backend.security.exception.InvalidCredentialsException;
import com.tbank.ttravels_backend.security.exception.UserAlreadyExistsException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordCredentialRepository credentialRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final TokenHashService tokenHashService;

    @Transactional
    public AuthResponse register(AuthRegisterRequest request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new UserAlreadyExistsException("Данный номер телефона уже зарегистрирован!");
        }

        User user = userRepository.save(User.builder()
                .phone(request.getPhone())
                .name(request.getName())
                .surname(request.getSurname())
                .build());

        credentialRepository.save(PasswordCredential.builder()
                .user(user)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build());

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(AuthLoginRequest request) {
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new InvalidCredentialsException("Неверные учетные данные"));

        PasswordCredential credential = credentialRepository.findByUserId(user.getId())
                .orElseThrow(() -> new InvalidCredentialsException("Неверные учетные данные"));

        if (!passwordEncoder.matches(request.getPassword(), credential.getPasswordHash())) {
            throw new InvalidCredentialsException("Неверные учетные данные");
        }

        return issueTokens(user);
    }

    private AuthResponse issueTokens(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .expiresAt(now().plus(jwtProperties.getRefreshTtl()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        String refreshJwt = jwtService.generateRefreshToken(refreshToken.getId(), user.getId());
        refreshToken.setTokenHash(tokenHashService.hash(refreshJwt));
        refreshTokenRepository.save(refreshToken);

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getPhone(), Map.of());

        return buildResponse(accessToken, refreshJwt);
    }

    private AuthResponse buildResponse(String accessToken, String refreshToken) {
        return new AuthResponse(
                accessToken,
                jwtProperties.getAccessTtl().toSeconds(),
                refreshToken,
                jwtProperties.getRefreshTtl().toSeconds(),
                "Bearer"
        );
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
