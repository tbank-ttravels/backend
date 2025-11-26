package com.tbank.ttravels_backend.service;

import com.tbank.ttravels_backend.dto.auth.*;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordCredentialRepository credentialRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenHashService tokenHashService;
    @Mock
    private TokenIssuer tokenIssuer;

    @InjectMocks
    private AccountService accountService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .phone("+78005553535")
                .name("Иван")
                .surname("Иванов")
                .build();
    }

    @Test
    void changePassword_shouldUpdateHashAndRevokeTokens() {
        ChangePasswordRequest request = new ChangePasswordRequest("old", "newPass");
        PasswordCredential credential = PasswordCredential.builder()
                .userId(1L)
                .user(user)
                .passwordHash("oldHash")
                .build();
        RefreshToken token1 = RefreshToken.builder().id(10L).user(user).revoked(false).build();
        RefreshToken token2 = RefreshToken.builder().id(11L).user(user).revoked(false).build();

        when(credentialRepository.findByUserId(1L)).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("old", "oldHash")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("newHash");
        when(refreshTokenRepository.findAllByUserIdAndRevokedFalse(1L)).thenReturn(List.of(token1, token2));

        accountService.changePassword(1L, request);

        assertThat(credential.getPasswordHash()).isEqualTo("newHash");
        verify(credentialRepository).save(credential);
        assertThat(token1.isRevoked()).isTrue();
        assertThat(token2.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(token1);
        verify(refreshTokenRepository).save(token2);
    }

    @Test
    void changePassword_shouldThrowWhenCredentialMissing() {
        when(credentialRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.changePassword(1L, new ChangePasswordRequest("old", "new")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(credentialRepository, never()).save(any());
        verify(refreshTokenRepository, never()).findAllByUserIdAndRevokedFalse(any());
    }

    @Test
    void changePassword_shouldThrowWhenCurrentInvalid() {
        PasswordCredential credential = PasswordCredential.builder()
                .userId(1L)
                .user(user)
                .passwordHash("oldHash")
                .build();
        when(credentialRepository.findByUserId(1L)).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("wrong", "oldHash")).thenReturn(false);

        assertThatThrownBy(() -> accountService.changePassword(1L, new ChangePasswordRequest("wrong", "new")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(credentialRepository, never()).save(any());
        verify(refreshTokenRepository, never()).findAllByUserIdAndRevokedFalse(any());
    }

    @Test
    void logout_shouldRevokeTokenWhenUserMatches() {
        LogoutRequest request = new LogoutRequest("refreshRaw");
        RefreshToken stored = RefreshToken.builder()
                .id(5L)
                .user(user)
                .tokenHash("hashed")
                .revoked(false)
                .build();
        when(tokenHashService.hash("refreshRaw")).thenReturn("hashed");
        when(refreshTokenRepository.findByTokenHash("hashed")).thenReturn(Optional.of(stored));

        accountService.logout(1L, request);

        assertThat(stored.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(stored);
    }

    @Test
    void logout_shouldThrowWhenTokenNotFound() {
        when(tokenHashService.hash("refreshRaw")).thenReturn("hashed");
        when(refreshTokenRepository.findByTokenHash("hashed")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.logout(1L, new LogoutRequest("refreshRaw")))
                .isInstanceOf(RefreshTokenNotFoundException.class);

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void logout_shouldThrowWhenUserMismatch() {
        LogoutRequest request = new LogoutRequest("refreshRaw");
        User other = User.builder().id(2L).build();
        RefreshToken stored = RefreshToken.builder()
                .id(5L)
                .user(other)
                .tokenHash("hashed")
                .revoked(false)
                .build();
        when(tokenHashService.hash("refreshRaw")).thenReturn("hashed");
        when(refreshTokenRepository.findByTokenHash("hashed")).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> accountService.logout(1L, request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refresh_shouldRotateToken() {
        RefreshRequest request = new RefreshRequest("refreshRaw");
        RefreshToken stored = RefreshToken.builder().id(5L).user(user).build();
        AuthResponse expected = new AuthResponse("acc", 100L, "ref", 200L);

        when(tokenHashService.hash("refreshRaw")).thenReturn("hashed");
        when(refreshTokenRepository.findByTokenHash("hashed")).thenReturn(Optional.of(stored));
        when(tokenIssuer.rotate(stored)).thenReturn(expected);

        AuthResponse response = accountService.refresh(request);

        assertThat(response).isEqualTo(expected);
    }

    @Test
    void refresh_shouldThrowWhenNotFound() {
        when(tokenHashService.hash("refreshRaw")).thenReturn("hashed");
        when(refreshTokenRepository.findByTokenHash("hashed")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.refresh(new RefreshRequest("refreshRaw")))
                .isInstanceOf(RefreshTokenNotFoundException.class);
    }

    @Test
    void getCurrentUser_shouldReturnAccountResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        AccountResponse response = accountService.getCurrentUser(1L);

        assertThat(response.getPhone()).isEqualTo(user.getPhone());
        assertThat(response.getName()).isEqualTo(user.getName());
        assertThat(response.getSurname()).isEqualTo(user.getSurname());
    }

    @Test
    void getCurrentUser_shouldThrowWhenNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getCurrentUser(1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void findUser_shouldReturnWhenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = accountService.findUser(1L);

        assertThat(result).isEqualTo(user);
    }

    @Test
    void findUser_shouldThrowWhenNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.findUser(1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void findUsers_shouldDelegateToRepository() {
        User u2 = User.builder().id(2L).build();
        when(userRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(user, u2));

        List<User> result = accountService.findUsers(Set.of(1L, 2L));

        assertThat(result).containsExactlyInAnyOrder(user, u2);
        verify(userRepository).findAllById(Set.of(1L, 2L));
    }

    @Test
    void findUserByPhone_shouldReturnUser() {
        when(userRepository.findByPhone("+78005553535")).thenReturn(Optional.of(user));

        User result = accountService.findUserByPhone("+78005553535");

        assertThat(result).isEqualTo(user);
    }

    @Test
    void findUserByPhone_shouldThrowWhenNotFound() {
        when(userRepository.findByPhone("+78005553535")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.findUserByPhone("+78005553535"))
                .isInstanceOf(UserNotFoundByPhoneException.class);
    }
}
