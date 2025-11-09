package com.tbank.ttravels_backend.entity;

import com.tbank.ttravels_backend.enums.HistoryType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name = "history")
@Getter
@Setter
@NoArgsConstructor
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_id")
    private Travel travel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    @Column(name = "date", updatable = false)
    @Setter(AccessLevel.NONE)
    private OffsetDateTime createdAt;

    @Column(name = "description")
    private String description;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private HistoryType type;


    // Фабричный метод создания истории
    public static History create(Travel travel, User author,
                                 String description, HistoryType type) {

        if (travel == null)
            throw new IllegalArgumentException("History creation failed: travel must not be null");

        if (author == null)
            throw new IllegalArgumentException("History creation failed: author must not be null");

        if (type == null)
            throw new IllegalArgumentException("History creation failed: type must not be null");

        History history = new History();

        history.travel = travel;
        history.author = author;
        history.description = description;
        history.type = type;

        return history;

    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
}