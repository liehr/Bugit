package de.tudl.playground.bugit.repositories;

import de.tudl.playground.bugit.models.Income;
import de.tudl.playground.bugit.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncomeRepository extends JpaRepository<Income, UUID> {
    Optional<List<Income>> findIncomesByUser(User user);
}
