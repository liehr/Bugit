package de.tudl.playground.bugit.controller;

import de.tudl.playground.bugit.dtos.requests.CreateIncomeRequest;
import de.tudl.playground.bugit.dtos.requests.DeleteIncomeRequest;
import de.tudl.playground.bugit.dtos.requests.UpdateIncomeRequest;
import de.tudl.playground.bugit.dtos.responses.IncomeResponse;
import de.tudl.playground.bugit.services.EncryptionService;
import de.tudl.playground.bugit.services.IncomeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/income")
@CrossOrigin
public class IncomeController {

    private final IncomeService incomeService;

    public IncomeController(IncomeService service) {
        this.incomeService = service;
    }

    @PostMapping("/create")
    public ResponseEntity<IncomeResponse> createIncome(@RequestBody CreateIncomeRequest request)
    {

        return new ResponseEntity<>(incomeService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<IncomeResponse> updateIncome(@RequestBody UpdateIncomeRequest request)
    {
        IncomeResponse response = incomeService.update(request);

        return response != null ? ResponseEntity.ok(response) : null;
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteIncome(@RequestBody DeleteIncomeRequest request)
    {
        String response = incomeService.delete(request);
        return response != null ? ResponseEntity.ok(response) : null;
    }

    @GetMapping("/")
    public ResponseEntity<List<IncomeResponse>> getAllIncomesByUser()
    {
        List<IncomeResponse> responses = incomeService.getAllIncomesByUser();
        return ResponseEntity.ok(responses);
    }
}
