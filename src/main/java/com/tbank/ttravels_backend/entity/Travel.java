package com.tbank.ttravels_backend.entity;

import com.tbank.ttravels_backend.enums.TravelStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private TravelStatus status;

    @OneToMany(cascade = CascadeType.ALL,
            mappedBy = "travel",
            orphanRemoval = true)
    private Set<TravelMember> travelMembers = new HashSet<>();

    @OneToMany(mappedBy = "travel",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Expense> expenses = new ArrayList<>();

    @OneToMany(mappedBy = "travel",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Transfer> transfers = new ArrayList<>();
}
