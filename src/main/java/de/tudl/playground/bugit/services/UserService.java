package de.tudl.playground.bugit.services;

import de.tudl.playground.bugit.dtos.LoginRequest;
import de.tudl.playground.bugit.dtos.RegisterRequest;
import de.tudl.playground.bugit.models.User;
import de.tudl.playground.bugit.repositories.UserRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    private final RequestStatusStore statusStore;

    public UserService(UserRepository userRepository, AuthenticationManager authenticationManager, JwtService jwtService, RequestStatusStore statusStore) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.statusStore = statusStore;
    }

    public void register(RegisterRequest request)
    {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));

        try {
            userRepository.save(user);
            statusStore.setStatus(request.getRequestId(), "SUCCESS");
        }
        catch (Exception e)
        {
            statusStore.setStatus(request.getRequestId(), "FAILED");
        }
    }

    @SneakyThrows
    public String verify(LoginRequest request) {

        Optional<User> userOptional = userRepository.findUserByEmail(request.email());

        if (userOptional.isEmpty())
        {
            log.error("User with Email {} not found.", request.email());
            throw new UsernameNotFoundException(request.email());
        }

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(userOptional.get().getUsername(), request.password()));


        return authentication.isAuthenticated() ? jwtService.generateToken(userOptional.get().getUsername()) : "Failed";
    }
}
