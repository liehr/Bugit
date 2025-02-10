package de.tudl.playground.bugit.models;

import de.tudl.playground.bugit.models.enums.RecurrenceInterval;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Spending {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String amount;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String date;

    @Column(nullable = false)
    private String isRecurring;

    @Enumerated(EnumType.STRING)
    private RecurrenceInterval recurrenceInterval;

    private String endDate;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
}
