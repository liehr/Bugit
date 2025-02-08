package de.tudl.playground.bugit.controller;

import de.tudl.playground.bugit.dtos.requests.budget.CreateBudgetRequest;
import de.tudl.playground.bugit.dtos.requests.budget.DeleteBudgetRequest;
import de.tudl.playground.bugit.dtos.requests.budget.UpdateBudgetRequest;
import de.tudl.playground.bugit.dtos.responses.BudgetResponse;
import de.tudl.playground.bugit.dtos.responses.BudgetResponseWithInvestments;
import de.tudl.playground.bugit.services.BudgetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/budget")
@CrossOrigin
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping("/")
    public ResponseEntity<BudgetResponse> getBudgetByUser() {
        return new ResponseEntity<>(budgetService.getBudgetByUser(), HttpStatus.OK);
    }

    @GetMapping("/invest")
    public ResponseEntity<BudgetResponseWithInvestments> getBudgetByUserWithInvestments() {
        return new ResponseEntity<>(budgetService.getBudgetWithInvestments(), HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<BudgetResponse> createBudget(@RequestBody CreateBudgetRequest request) {
        return new ResponseEntity<>(budgetService.createBudget(request), HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<BudgetResponse> updateBudget(@RequestBody UpdateBudgetRequest request) {
        return new ResponseEntity<>(budgetService.updateBudget(request), HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteBudget(@RequestBody DeleteBudgetRequest request)
    {
        return new ResponseEntity<>(budgetService.deleteBudget(request), HttpStatus.OK);
    }
}
