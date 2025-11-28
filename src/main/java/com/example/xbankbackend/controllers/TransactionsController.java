package com.example.xbankbackend.controllers;

import com.example.xbankbackend.dtos.RecentTransactionsDTO;
import com.example.xbankbackend.models.Transaction;
import com.example.xbankbackend.services.TransactionsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@AllArgsConstructor
@Log4j2
@RestController
@CrossOrigin
@RequestMapping("/transactions")
public class TransactionsController {

    private TransactionsService transactionsService;

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@Valid @RequestBody Transaction deposit) {
        log.info("Processing deposit: {}", deposit);
        transactionsService.deposit(deposit);
        return ResponseEntity.ok(deposit);
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@Valid @RequestBody Transaction transfer) {
        log.info("Processing transfer: {}", transfer);
        transactionsService.transfer(transfer);
        return ResponseEntity.ok(transfer);
    }

    @PostMapping("/payment")
    public ResponseEntity<?> payment(@Valid @RequestBody Transaction payment) {
        log.info("Processing payment: {}", payment);
        transactionsService.pay(payment);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/getProfile-recent-transactions/{accountId}")
    public ResponseEntity<RecentTransactionsDTO> getRecent(@PathVariable UUID accountId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "5") int size) {
        log.info("Getting transactions for account {} on page {} of size {}", accountId, page, size);
        RecentTransactionsDTO recentTransactions = transactionsService.getRecent(accountId, page, size);
        return ResponseEntity.ok(recentTransactions);
    }
}
