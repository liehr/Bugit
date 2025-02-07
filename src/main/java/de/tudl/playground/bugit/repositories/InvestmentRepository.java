package de.tudl.playground.bugit.repositories;

import de.tudl.playground.bugit.models.Investment;
import de.tudl.playground.bugit.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvestmentRepository extends JpaRepository<Investment, UUID> {
    Optional<List<Investment>> findAllByUser(User user);
}
