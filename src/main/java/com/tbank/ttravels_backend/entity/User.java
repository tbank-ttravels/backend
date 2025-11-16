package com.tbank.ttravels_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phone;

    private String name;

    private String surname;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private PasswordCredential password;
}
