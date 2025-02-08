package de.tudl.playground.bugit.controller;

import de.tudl.playground.bugit.dtos.requests.investment.CreateInvestmentRequest;
import de.tudl.playground.bugit.dtos.requests.investment.DeleteInvestmentRequest;
import de.tudl.playground.bugit.dtos.requests.investment.UpdateInvestmentRequest;
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

    @PutMapping("/update")
    public ResponseEntity<InvestmentResponse> updateInvestment(@RequestBody UpdateInvestmentRequest request)
    {
        InvestmentResponse investmentResponse = investmentService.updateInvestment(request);

        return investmentResponse != null ? ResponseEntity.ok(investmentResponse) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteInvestment(@RequestBody DeleteInvestmentRequest request)
    {
        String response = investmentService.deleteInvestment(request);

        return response != null ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }

    @GetMapping("/")
    public ResponseEntity<List<InvestmentResponse>> getAllInvestments()
    {
        return new ResponseEntity<>(investmentService.getAllInvestmentsByUser(), HttpStatus.OK);
    }
}
