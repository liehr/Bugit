package de.tudl.playground.bugit.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Investment
{
    @Id
    private UUID id;

    @Column(nullable = false)
    private String asset;

    @Column(nullable = false)
    private String amount;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String liquidity;

    @Column(nullable = false)
    private String recurring;

    @Column(nullable = false)
    private String monthlyInvest;

    @ManyToOne
    @JoinColumn(name = "budget_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Budget budget;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;
}
