package com.tbank.ttravels_backend.service.security;


import com.tbank.ttravels_backend.entity.PasswordCredential;
import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.repository.PasswordCredentialRepository;
import com.tbank.ttravels_backend.repository.UserRepository;
import com.tbank.ttravels_backend.security.UserDetailsServiceImpl;
import com.tbank.ttravels_backend.security.UserPrincipal;
import com.tbank.ttravels_backend.service.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordCredentialRepository credentialRepository;

    @InjectMocks
    private UserDetailsServiceImpl service;

    @Test
    void loadUserByUsername_success() {

        // === Given ===
        String phone = "+70001112233";
        long userId = 42;
        String passwordHash = "$2a$10$B6YjH5T8mN9P0Q1R2S3T4U";
        User user = TestDataFactory.user(userId, "+70001112233");

        PasswordCredential credential = TestDataFactory.passwordCredential(passwordHash, user);


        // === Mocking ===
        doReturn(Optional.of(user)).when(userRepository).findByPhone(phone);
        doReturn(Optional.of(credential)).when(credentialRepository).findByUserId(userId);


        // === When ===
        UserDetails actual = service.loadUserByUsername(phone);


        // === Then ===
        assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> assertThat(actual).isInstanceOf(UserPrincipal.class),
                () -> {
                    UserPrincipal principal = (UserPrincipal) actual;
                    assertThat(principal.getId()).isEqualTo(userId);
                    assertThat(principal.getUsername()).isEqualTo(phone);
                    assertThat(principal.getPassword()).isEqualTo(passwordHash);

                    Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();
                    assertThat(authorities).isNotNull();
                    assertThat(authorities.size()).isEqualTo(1);
                    assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_USER");
                }
        );


        // === Verify ===
        verify(userRepository).findByPhone(phone);
        verify(credentialRepository).findByUserId(userId);
    }

    @Test
    void loadUserByUsername_userNotFound() {

        // === Given ===
        String phone = "+70001112233";


        // === Mocking ===
        doReturn(Optional.empty()).when(userRepository).findByPhone(phone);


        // === When & Then ===
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername(phone));


        // === Verify ===
        verify(userRepository).findByPhone(phone);
        verify(credentialRepository, never()).findByUserId(anyLong());
    }

    @Test
    void loadUserByUsername_noPasswordCredential() {

        // === Given ===
        String phone = "+70001112233";
        long userId = 42;
        User user = TestDataFactory.user(userId, phone);


        // === Mocking ===
        doReturn(Optional.of(user)).when(userRepository).findByPhone(phone);
        doReturn(Optional.empty()).when(credentialRepository).findByUserId(userId);


        // === When & Then ===
        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername(phone)
        );


        // === Verify ===
        verify(userRepository).findByPhone(phone);
        verify(credentialRepository).findByUserId(userId);
    }
}
