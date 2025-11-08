package com.tbank.ttravels_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "password")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PasswordCredential {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "password_hash")
    private String passwordHash;
}
