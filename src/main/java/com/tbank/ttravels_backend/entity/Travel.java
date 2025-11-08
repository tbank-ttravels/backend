package com.tbank.ttravels_backend.entity;

import com.tbank.ttravels_backend.enums.TravelStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "travels")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Travel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private OffsetDateTime startDate;

    private OffsetDateTime endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Enumerated(EnumType.STRING)
    private TravelStatus status;
}
