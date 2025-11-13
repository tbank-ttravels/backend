package com.tbank.ttravels_backend.security;

import com.tbank.ttravels_backend.dto.auth.AuthLoginRequest;
import com.tbank.ttravels_backend.dto.auth.AuthRegisterRequest;
import com.tbank.ttravels_backend.dto.auth.AuthResponse;
import com.tbank.ttravels_backend.entity.PasswordCredential;
import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.exception.InvalidCredentialsException;
import com.tbank.ttravels_backend.exception.UserAlreadyExistsException;
import com.tbank.ttravels_backend.repository.PasswordCredentialRepository;
import com.tbank.ttravels_backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordCredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;

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

        return tokenIssuer.issue(user);
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

        return tokenIssuer.issue(user);
    }
}
