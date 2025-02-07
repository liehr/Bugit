package de.tudl.playground.bugit.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @ManyToOne
    @JoinColumn(name = "budget_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Budget budget;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;
}
