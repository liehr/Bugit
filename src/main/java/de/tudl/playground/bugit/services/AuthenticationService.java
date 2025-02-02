package de.tudl.playground.bugit.services;

import de.tudl.playground.bugit.models.User;
import de.tudl.playground.bugit.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
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
}
