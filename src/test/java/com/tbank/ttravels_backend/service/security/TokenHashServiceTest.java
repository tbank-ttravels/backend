package com.tbank.ttravels_backend.service.security;

import com.tbank.ttravels_backend.security.TokenHashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TokenHashServiceTest {

    private TokenHashService service;

    @BeforeEach
    void setUp() {
        service = new TokenHashService();
    }

    @Test
    void hash_returnsNonNull() {
        String token = "my-token";
        String actualHash = service.hash(token);

        assertThat(actualHash).isNotNull();
    }

    @Test
    void hash_returns64CharHex() {
        String token = "my-token";
        String actualHash = service.hash(token);

        assertThat(actualHash).hasSize(64);
    }

    @Test
    void hash_isDeterministic() {
        String token = "my-token";
        String hash1 = service.hash(token);
        String hash2 = service.hash(token);

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void hash_differsForDifferentInputs() {
        String token1 = "my-token";
        String token2 = "other-token";
        String hash1 = service.hash(token1);
        String hash2 = service.hash(token2);

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void hash_throwsIllegalStateExceptionIfSHA256Unavailable() {
        TokenHashService faultyService = new TokenHashService() {
            @Override
            public String hash(String token) {
                try {
                    MessageDigest.getInstance("FAKE-ALGO");
                    return super.hash(token);
                } catch (NoSuchAlgorithmException e) {
                    throw new IllegalStateException("SHA-256 not available", e);
                }
            }
        };

        assertThrows(IllegalStateException.class, () -> faultyService.hash("any-token"));
    }
}
