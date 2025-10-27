package com.example.xbankbackend.controllers;

import com.example.xbankbackend.models.Transaction;
import com.example.xbankbackend.services.TransactionsService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Log4j2
@RestController
@CrossOrigin
@RequestMapping("/api/transactions")
public class TransactionsController {
    @Autowired
    private TransactionsService transactionsService;

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@Valid @RequestBody Transaction deposit) {
        log.info(deposit);
        try {
            transactionsService.depositAccount(deposit);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
        return ResponseEntity.ok(deposit.toString());
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@Valid @RequestBody Transaction transfer) {
        log.info(transfer);
        try {
            transactionsService.transferMoney(transfer);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
        return ResponseEntity.ok(transfer.toString());
    }

    @PostMapping("/payment")
    public ResponseEntity<?> payment(@Valid @RequestBody Transaction payment) {
        log.info(payment);
        try {
            transactionsService.pay(payment);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
        return ResponseEntity.ok(payment.toString());
    }
}
