package de.tudl.playground.bugit.controller;

import de.tudl.playground.bugit.dtos.CreateIncomeRequest;
import de.tudl.playground.bugit.dtos.IncomeResponse;
import de.tudl.playground.bugit.services.IncomeService;
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
        IncomeResponse response = incomeService.create(request);
        return response != null ? ResponseEntity.ok(response) : null;
    }

    @GetMapping("/")
    public ResponseEntity<List<IncomeResponse>> getAllIncomesByUser()
    {
        List<IncomeResponse> responses = incomeService.getAllIncomesByUser();
        return ResponseEntity.ok(responses);
    }
}
