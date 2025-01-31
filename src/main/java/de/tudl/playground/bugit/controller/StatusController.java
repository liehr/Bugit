package de.tudl.playground.bugit.controller;

import de.tudl.playground.bugit.services.RequestStatusStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/status")
@CrossOrigin
public class StatusController {

    private final RequestStatusStore statusStore;

    public StatusController(RequestStatusStore statusStore) {
        this.statusStore = statusStore;
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<String> getStatus(@PathVariable String requestId) {
        String status = statusStore.getStatus(requestId);
        return status != null ? ResponseEntity.ok(status) : ResponseEntity.notFound().build();
    }
}

