package com.tbank.ttravels_backend.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class UserPrincipal implements UserDetails {
    private final Long id;
    private final String phone;
    private final String passwordHash;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String phone, String passwordHash,
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.authorities = authorities == null
                ? Collections.singleton(() -> "ROLE_USER")
                : authorities;
    }

    @Override
    public String getUsername() {
        return phone;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }
}