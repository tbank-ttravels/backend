package com.tbank.ttravels_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "token_hash")
    private String tokenHash;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "revoked")
    private boolean revoked;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    void onPersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}