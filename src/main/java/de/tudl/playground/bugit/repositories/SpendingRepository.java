package de.tudl.playground.bugit.repositories;

import de.tudl.playground.bugit.models.Spending;
import de.tudl.playground.bugit.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpendingRepository extends JpaRepository<Spending, UUID> {
    Optional<List<Spending>> findAllSpendingsByUser(User user);
}
