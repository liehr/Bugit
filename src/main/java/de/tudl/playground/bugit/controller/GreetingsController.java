package de.tudl.playground.bugit.controller;

import de.tudl.playground.bugit.dtos.ContentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/greet")
public class GreetingsController {

    @GetMapping("/here")
    public ResponseEntity<ContentDto> greet() {
        return ResponseEntity.ok(new ContentDto("I'm up and running!"));
    }
}
