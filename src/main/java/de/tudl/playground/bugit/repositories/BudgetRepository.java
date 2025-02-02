package de.tudl.playground.bugit.repositories;

import de.tudl.playground.bugit.models.Budget;
import de.tudl.playground.bugit.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    Optional<Budget> findBudgetByUser(User user);
}
