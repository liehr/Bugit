package de.tudl.playground.bugit.repositories;

import de.tudl.playground.bugit.models.Budget;
import de.tudl.playground.bugit.models.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {


    Optional<Budget> findBudgetByUser(User user);
    
    @Query("""
        SELECT DISTINCT b
        FROM Budget b
        LEFT JOIN FETCH b.investments i
        WHERE b.user = :user
    """)
    Optional<Budget> findBudgetWithInvestmentsByUser(@Param("user") User user);
}
