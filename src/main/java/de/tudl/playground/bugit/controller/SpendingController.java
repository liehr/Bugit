package de.tudl.playground.bugit.controller;

import de.tudl.playground.bugit.dtos.requests.spending.CreateSpendingRequest;
import de.tudl.playground.bugit.dtos.responses.SpendingResponse;
import de.tudl.playground.bugit.services.SpendingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spending")
@CrossOrigin
public class SpendingController {
    private final SpendingService spendingService;

    public SpendingController(SpendingService spendingService) {
        this.spendingService = spendingService;
    }

    @PostMapping("/create")
    public ResponseEntity<SpendingResponse> createSpending(@RequestBody CreateSpendingRequest request) {
        return  new ResponseEntity<>(spendingService.createSpending(request), HttpStatus.CREATED);
    }

    @GetMapping("/")
    public ResponseEntity<List<SpendingResponse>> getSpendings() {
        return new ResponseEntity<>(spendingService.getAllSpendingsByUser(), HttpStatus.OK);
    }
}
