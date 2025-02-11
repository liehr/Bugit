package de.tudl.playground.bugit.controller;

import de.tudl.playground.bugit.dtos.requests.spending.CreateSpendingRequest;
import de.tudl.playground.bugit.dtos.requests.spending.DeleteSpendingRequest;
import de.tudl.playground.bugit.dtos.requests.spending.UpdateSpendingRequest;
import de.tudl.playground.bugit.dtos.responses.SpendingResponse;
import de.tudl.playground.bugit.services.SpendingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
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

    @PutMapping("/update")
    public ResponseEntity<SpendingResponse> updateSpending(@RequestBody UpdateSpendingRequest request) {
        SpendingResponse spendingResponse = spendingService.updateSpending(request);

        return spendingResponse != null ? new ResponseEntity<>(spendingResponse, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteSpending(@RequestBody DeleteSpendingRequest request) {

        String response = spendingService.deleteSpending(request);

        return response != null ? new ResponseEntity<>(response, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/")
    public ResponseEntity<List<SpendingResponse>> getSpendings() {
        return new ResponseEntity<>(spendingService.getAllSpendingsByUser(), HttpStatus.OK);
    }
}
