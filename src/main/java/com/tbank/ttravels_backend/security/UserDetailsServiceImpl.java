package com.tbank.ttravels_backend.security;

import com.tbank.ttravels_backend.entity.PasswordCredential;
import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.repository.PasswordCredentialRepository;
import com.tbank.ttravels_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordCredentialRepository credentialRepository;

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String passwordHash = credentialRepository.findByUserId(user.getId())
                .map(PasswordCredential::getPasswordHash)
                .orElseThrow(() -> new UsernameNotFoundException("Password not configured"));

        return new UserPrincipal(
                user.getId(),
                user.getPhone(),
                passwordHash,
                null
        );
    }
}
