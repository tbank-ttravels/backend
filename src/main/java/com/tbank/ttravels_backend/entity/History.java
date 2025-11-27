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
@NoArgsConstructor
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_id")
    private Travel travel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    @Column(name = "date", updatable = false)
    private OffsetDateTime createdAt;

    @Setter
    @Column(name = "description")
    private String description;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private HistoryType type;


    // Фабричный метод создания истории
    public static History create(Travel travel, User author,
                                 String description, HistoryType type) {

        History history = new History();

        history.setAuthor(author);
        history.setTravel(travel);
        history.setType(type);
        history.description = description;

        return history;

    }

    public void setTravel(Travel travel) {

        if (travel == null)
            throw new IllegalArgumentException("Не удалось создать историю: поездка не может быть null");

        this.travel = travel;
    }

    public void setAuthor(User author) {

        if (author == null)
            throw new IllegalArgumentException("Не удалось создать историю: автор не может быть null");

        this.author = author;
    }

    public void setType(HistoryType type) {

        if (type == null)
            throw new IllegalArgumentException("Не удалось создать историю: тип не может быть null");

        this.type = type;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
}