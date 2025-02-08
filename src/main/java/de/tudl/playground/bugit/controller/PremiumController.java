package de.tudl.playground.bugit.controller;

import de.tudl.playground.bugit.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/premium")
@CrossOrigin
public class PremiumController {
    private final UserService userService;

    public PremiumController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user")
    public ResponseEntity<Boolean> isPremium() {
        return new ResponseEntity<>(userService.isUserPremium(), HttpStatus.OK);
    }
}
