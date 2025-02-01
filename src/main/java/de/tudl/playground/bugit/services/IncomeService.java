package de.tudl.playground.bugit.services;

import de.tudl.playground.bugit.dtos.CreateIncomeRequest;
import de.tudl.playground.bugit.dtos.IncomeResponse;
import de.tudl.playground.bugit.models.Income;
import de.tudl.playground.bugit.models.User;
import de.tudl.playground.bugit.repositories.IncomeRepository;
import de.tudl.playground.bugit.repositories.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class IncomeService {
    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;

    private final JwtService jwtService;

    public IncomeService(IncomeRepository incomeRepository, UserRepository userRepository, JwtService jwtService) {
        this.incomeRepository = incomeRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public IncomeResponse create(CreateIncomeRequest request) {

        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        if (authentication.isAuthenticated())
        {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Optional<User> userOptional = userRepository.findUserByUsername(userDetails.getUsername());

            if (userOptional.isPresent())
            {
                Income income = new Income();
                income.setId(UUID.randomUUID());
                income.setSource(request.source());
                income.setAmount(request.amount());
                income.setUser(userOptional.get());

                incomeRepository.save(income);

                return new IncomeResponse(income.getSource(), income.getAmount());
            }
        }

        return null;
    }

    public List<IncomeResponse> getAllIncomesByUser() {
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        List<IncomeResponse> incomeResponses = new ArrayList<>();

        if (authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Optional<User> userOptional = userRepository.findUserByUsername(userDetails.getUsername());

            if (userOptional.isPresent()) {
                Optional<List<Income>> incomes = incomeRepository.findIncomesByUser(userOptional.get());

                incomes.ifPresent(incomeList -> incomeList.forEach(e ->
                        incomeResponses.add(new IncomeResponse(e.getSource(), e.getAmount()))));
            }
        }

        return incomeResponses;
    }
}
