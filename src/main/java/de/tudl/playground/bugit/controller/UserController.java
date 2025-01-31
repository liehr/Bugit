package de.tudl.playground.bugit.controller;

import de.tudl.playground.bugit.dtos.LoginRequest;
import de.tudl.playground.bugit.dtos.RegisterRequest;
import de.tudl.playground.bugit.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {

        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request)
    {
        String requestId = UUID.randomUUID().toString();
        request.setRequestId(requestId);

        userService.register(request);

        return ResponseEntity.ok("Request ID: " + requestId);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request)
    {
        return ResponseEntity.ok(userService.verify(request));
    }
}
