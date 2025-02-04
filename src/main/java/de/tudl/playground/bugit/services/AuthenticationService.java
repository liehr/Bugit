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
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found.");
        }

        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userRepository.findUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "User with username " + userDetails.getUsername() + " not found."));
        }

        throw new IllegalStateException("User details not found in authentication.");
    }
}
