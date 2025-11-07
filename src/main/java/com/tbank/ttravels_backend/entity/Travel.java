package com.tbank.ttravels_backend.entity;

import com.tbank.ttravels_backend.enums.TravelStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "travels", indexes = { @Index(name = "idx_travels_owner", columnList = "owner_id") } )
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Travel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "start_date", nullable = false)
    private OffsetDateTime startDate;

    @Column(name = "end_date")
    private OffsetDateTime endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false, foreignKey = @ForeignKey(name = "fk_travels_owner"))
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TravelStatus status;
}
