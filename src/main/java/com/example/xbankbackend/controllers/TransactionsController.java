package com.example.xbankbackend.controllers;

import com.example.xbankbackend.dtos.requests.CreateTransactionRequest;
import com.example.xbankbackend.dtos.responses.RecentTransactionsResponse;
import com.example.xbankbackend.dtos.responses.TransactionResponse;
import com.example.xbankbackend.jwt.SecurityUtil;
import com.example.xbankbackend.mappers.TransactionMapper;
import com.example.xbankbackend.models.Transaction;
import com.example.xbankbackend.services.transaction.TransactionsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Log4j2
@RestController
@CrossOrigin
@RequestMapping("/transactions")
public class TransactionsController {

    private TransactionsService transactionsService;
    private TransactionMapper transactionMapper;
    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody CreateTransactionRequest transactionRequest) {
        log.info("Processing deposit: {}", transactionRequest);
        UUID userId = SecurityUtil.getCurrentUserId();

        Transaction deposit = transactionMapper.requestToTransaction(transactionRequest);
        TransactionResponse response = transactionsService.deposit(deposit, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody CreateTransactionRequest transactionRequest) {
        log.info("Processing transfer: {}", transactionRequest);
        UUID userId = SecurityUtil.getCurrentUserId();

        Transaction transfer = transactionMapper.requestToTransaction(transactionRequest);
        TransactionResponse response = transactionsService. transfer(transfer, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/payment")
    public ResponseEntity<Transaction> payment(@Valid @RequestBody CreateTransactionRequest transactionRequest) {
        log.info("Processing payment: {}", transactionRequest);
        UUID userId = SecurityUtil.getCurrentUserId();

        Transaction payment = transactionMapper.requestToTransaction(transactionRequest);
        transactionsService.pay(payment, userId);
        return ResponseEntity.ok(payment);
    }

    // CURRENT USER or ADMIN

    @GetMapping("/get/{accountId}")
    @PreAuthorize("hasRole('ADMIN') or @ownershipService.isAccountOwner(#accountId, authentication)")
    public ResponseEntity<RecentTransactionsResponse> getRecent(@PathVariable UUID accountId,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "5") int size) {
        log.info("Getting transactions for account {} on page {} of size {}", accountId, page, size);

        RecentTransactionsResponse recentTransactions = transactionsService.getRecent(accountId, page, size);
        return ResponseEntity.ok(recentTransactions);
    }

    @GetMapping("/{accountId}/{categoryCode}")
    @PreAuthorize("hasRole('ADMIN') or @ownershipService.isAccountOwner(#accountId, authentication)")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByCategory(@PathVariable UUID accountId,
                                                                               @PathVariable String categoryCode) {
        log.info("Getting transactions with category {} for account {}", categoryCode, accountId);

        List<Transaction> transactions = transactionsService.getTransactionsByCategory(accountId, categoryCode);
        List<TransactionResponse> responses = transactionMapper.transactionsToResponses(transactions);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/{transactionId}")
    @PreAuthorize("hasRole('ADMIN') or @ownershipService.isTransactionOwner(#transactionId, authentication)")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable UUID transactionId) {
        log.info("Getting transaction with id {}", transactionId);

        TransactionResponse transactionResponse = transactionsService.getTransaction(transactionId);
        return ResponseEntity.status(HttpStatus.OK).body(transactionResponse);
    }

    // ADMIN-only

    @PutMapping("/cancel/{transactionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancelTransaction(@PathVariable UUID transactionId) {
        log.info("Cancelling transaction with id {}", transactionId);

        transactionsService.cancelTransaction(transactionId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
