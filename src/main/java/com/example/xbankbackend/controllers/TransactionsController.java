package com.example.xbankbackend.controllers;

import com.example.xbankbackend.models.Transaction;
import com.example.xbankbackend.services.TransactionsService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@CrossOrigin
@RequestMapping("/api/transactions")
public class TransactionsController {
    @Autowired
    private TransactionsService transactionsService;

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@Valid @RequestBody Transaction deposit) {
        log.info("Processing deposit: {}", deposit);
        transactionsService.depositAccount(deposit);
        return ResponseEntity.ok(deposit);
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@Valid @RequestBody Transaction transfer) {
        log.info("Processing transfer: {}", transfer);
        transactionsService.transferMoney(transfer);
        return ResponseEntity.ok(transfer);
    }

    @PostMapping("/payment")
    public ResponseEntity<?> payment(@Valid @RequestBody Transaction payment) {
        log.info("Processing payment: {}", payment);
        transactionsService.pay(payment);
        return ResponseEntity.ok(payment);
    }
}
