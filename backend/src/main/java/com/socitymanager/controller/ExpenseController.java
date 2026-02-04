package com.socitymanager.controller;

import com.socitymanager.model.ExpenseRequest;
import com.socitymanager.service.ReceiptService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*")
public class ExpenseController {
    private final ReceiptService receiptService;

    public ExpenseController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createExpense(@Valid @RequestBody ExpenseRequest request) {
        String receiptNumber = receiptService.createReceiptAndEmail(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("receiptNumber", receiptNumber, "message", "Expense recorded and receipt sent."));
    }
}
