package de.tudl.playground.bugit.services;

import de.tudl.playground.bugit.models.User;
import de.tudl.playground.bugit.models.UserPrincipal;
import de.tudl.playground.bugit.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository repository;

    public MyUserDetailsService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<User> userOptional = repository.findUserByUsername(username);

        if (userOptional.isEmpty())
        {
            log.error("User with username {} not found!", username);
            throw new UsernameNotFoundException(username);
        }



        return new UserPrincipal(userOptional.get());
    }
}
