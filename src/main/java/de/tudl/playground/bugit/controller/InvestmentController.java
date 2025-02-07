package de.tudl.playground.bugit.controller;

import de.tudl.playground.bugit.dtos.requests.CreateInvestmentRequest;
import de.tudl.playground.bugit.dtos.responses.InvestmentResponse;
import de.tudl.playground.bugit.services.InvestmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/investment")
@CrossOrigin
public class InvestmentController {

    private final InvestmentService investmentService;

    public InvestmentController(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    @PostMapping("/create")
    public ResponseEntity<InvestmentResponse> createInvestment(@RequestBody CreateInvestmentRequest request)
    {
        return new ResponseEntity<>(investmentService.createInvestment(request), HttpStatus.CREATED);
    }

    @GetMapping("/")
    public ResponseEntity<List<InvestmentResponse>> getAllInvestments()
    {
        return new ResponseEntity<>(investmentService.getAllInvestmentsByUser(), HttpStatus.OK);
    }
}
