package com.tbank.ttravels_backend.entity;

import com.tbank.ttravels_backend.enums.MemberStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "travel_members",
        uniqueConstraints = {@UniqueConstraint(name = "uq_travel_members_user_travel", columnNames = {"id_user", "id_travel"})},
        indexes = {@Index(name = "idx_travel_members_travel", columnList = "id_travel")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TravelMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false, foreignKey = @ForeignKey(name = "fk_travel_members_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_travel", nullable = false, foreignKey = @ForeignKey(name = "fk_travel_members_travel"))
    private Travel travel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;
}
