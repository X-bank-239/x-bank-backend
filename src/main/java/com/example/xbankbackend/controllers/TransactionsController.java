package com.example.xbankbackend.controllers;

import com.example.xbankbackend.dtos.requests.CreateTransactionRequest;
import com.example.xbankbackend.dtos.responses.RecentTransactionsResponse;
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
    public ResponseEntity<Transaction> deposit(@Valid @RequestBody CreateTransactionRequest transactionRequest) {
        log.info("Processing deposit: {}", transactionRequest);

        Transaction deposit = convertRequest(transactionRequest);
        transactionsService.deposit(deposit);
        return ResponseEntity.ok(deposit);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transfer(@Valid @RequestBody CreateTransactionRequest transactionRequest) {
        log.info("Processing transfer: {}", transactionRequest);

        Transaction transfer = convertRequest(transactionRequest);
        transactionsService.transfer(transfer);
        return ResponseEntity.ok(transfer);
    }

    @PostMapping("/payment")
    public ResponseEntity<Transaction> payment(@Valid @RequestBody CreateTransactionRequest transactionRequest) {
        log.info("Processing payment: {}", transactionRequest);

        Transaction payment = convertRequest(transactionRequest);
        transactionsService.pay(payment);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/get-recent/{accountId}")
    public ResponseEntity<RecentTransactionsResponse> getRecent(@PathVariable UUID accountId,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "5") int size) {
        log.info("Getting transactions for account {} on page {} of size {}", accountId, page, size);
        RecentTransactionsResponse recentTransactions = transactionsService.getRecent(accountId, page, size);
        return ResponseEntity.ok(recentTransactions);
    }

    private Transaction convertRequest(CreateTransactionRequest transactionRequest) {
        Transaction transaction = new Transaction();

        transaction.setTransactionType(transactionRequest.getTransactionType());
        transaction.setAmount(transactionRequest.getAmount());
        transaction.setCurrency(transactionRequest.getCurrency());
        transaction.setSenderId(transactionRequest.getSenderId());
        transaction.setReceiverId(transactionRequest.getReceiverId());
        transaction.setComment(transactionRequest.getComment());

        return transaction;
    }
}
