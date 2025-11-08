package com.tbank.ttravels_backend.entity;

import com.tbank.ttravels_backend.enums.MemberStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "travel_members")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TravelMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_travel")
    private Travel travel;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;
}
