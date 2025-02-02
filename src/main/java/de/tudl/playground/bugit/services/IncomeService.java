package de.tudl.playground.bugit.services;

import de.tudl.playground.bugit.dtos.CreateIncomeRequest;
import de.tudl.playground.bugit.dtos.IncomeResponse;
import de.tudl.playground.bugit.models.Income;
import de.tudl.playground.bugit.models.User;
import de.tudl.playground.bugit.repositories.IncomeRepository;
import de.tudl.playground.bugit.repositories.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;

    public IncomeService(IncomeRepository incomeRepository, UserRepository userRepository) {
        this.incomeRepository = incomeRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves the currently authenticated user.
     *
     * @return the User entity corresponding to the current user.
     * @throws IllegalStateException if no authentication is present or user details cannot be extracted.
     * @throws UsernameNotFoundException if the user is not found in the repository.
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found.");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails)) {
            throw new IllegalStateException("User details not found in authentication.");
        }

        String username = ((UserDetails) principal).getUsername();
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with username " + username + " not found."));
    }

    /**
     * Creates an Income record for the current user.
     *
     * @param request the income creation request.
     * @return the response DTO containing the income details.
     */
    public IncomeResponse create(CreateIncomeRequest request) {
        User currentUser = getCurrentUser();

        Income income = new Income();
        income.setId(UUID.randomUUID());
        income.setSource(request.source());
        income.setAmount(request.amount());
        income.setUser(currentUser);

        incomeRepository.save(income);

        return new IncomeResponse(income.getSource(), income.getAmount());
    }

    /**
     * Retrieves all incomes associated with the currently authenticated user.
     *
     * @return a list of IncomeResponse DTOs.
     */
    public List<IncomeResponse> getAllIncomesByUser() {
        User currentUser = getCurrentUser();

        // If your repository method returns an Optional<List<Income>>, use orElse with an empty list.
        List<Income> incomes = incomeRepository.findIncomesByUser(currentUser)
                .orElse(Collections.emptyList());

        return incomes.stream()
                .map(income -> new IncomeResponse(income.getSource(), income.getAmount()))
                .collect(Collectors.toList());
    }
}
