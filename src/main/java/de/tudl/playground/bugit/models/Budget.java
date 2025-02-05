package de.tudl.playground.bugit.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Budget {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String amount;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
