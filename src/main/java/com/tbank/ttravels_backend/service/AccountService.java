package com.tbank.ttravels_backend.service;

import com.tbank.ttravels_backend.dto.auth.AccountResponse;
import com.tbank.ttravels_backend.dto.auth.AuthResponse;
import com.tbank.ttravels_backend.dto.auth.ChangePasswordRequest;
import com.tbank.ttravels_backend.dto.auth.RefreshOrLogoutRequest;
import com.tbank.ttravels_backend.entity.PasswordCredential;
import com.tbank.ttravels_backend.entity.RefreshToken;
import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.exception.InvalidCredentialsException;
import com.tbank.ttravels_backend.exception.RefreshTokenNotFoundException;
import com.tbank.ttravels_backend.exception.UserNotFoundByPhoneException;
import com.tbank.ttravels_backend.exception.UserNotFoundException;
import com.tbank.ttravels_backend.repository.PasswordCredentialRepository;
import com.tbank.ttravels_backend.repository.RefreshTokenRepository;
import com.tbank.ttravels_backend.repository.UserRepository;
import com.tbank.ttravels_backend.security.TokenHashService;
import com.tbank.ttravels_backend.security.TokenIssuer;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepository;
    private final PasswordCredentialRepository credentialRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenHashService tokenHashService;
    private final TokenIssuer tokenIssuer;

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        PasswordCredential credential = credentialRepository.findByUserId(userId)
                .orElseThrow(() -> new InvalidCredentialsException("Пароль для пользователя не найден"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), credential.getPasswordHash())) {
            throw new InvalidCredentialsException("Неверный текущий пароль");
        }

        credential.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        credentialRepository.save(credential);

        refreshTokenRepository.findAllByUserIdAndRevokedFalse(userId)
                .forEach(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional
    public void logout(Long userId, RefreshOrLogoutRequest request) {
        String tokenHash = tokenHashService.hash(request.getRefreshToken());
        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token не найден"));

        if (!token.getUser().getId().equals(userId)) {
            throw new InvalidCredentialsException("Токен не принадлежит пользователю");
        }

        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    @Transactional
    public AuthResponse refresh(RefreshOrLogoutRequest request) {
        String tokenHash = tokenHashService.hash(request.getRefreshToken());
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token не найден"));

        return tokenIssuer.rotate(storedToken);
    }

    public AccountResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id = " + userId + " не найден"));
        return new AccountResponse(user.getPhone(), user.getName(), user.getSurname());
    }

    public User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id = " + userId + " не найден"));
    }

    public List<User> findUsers(Set<Long> userIds) {
        return this.userRepository.findAllById(userIds);
    }

    public User findUserByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() ->
                        new UserNotFoundByPhoneException("Пользователь с phone = " + phone + " не найден"));
    }
}
