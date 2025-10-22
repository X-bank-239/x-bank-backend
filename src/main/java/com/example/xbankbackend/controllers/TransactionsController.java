package com.example.xbankbackend.controllers;

import com.example.xbankbackend.models.Payment;
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
@RequestMapping("/api")
public class TransactionsController {
    @Autowired
    private TransactionsService transactionsService;

    @PostMapping("/topup")
    public ResponseEntity<?> topUp(@Valid @RequestBody Payment payment) {
        try {
            log.info(payment);
            transactionsService.topUpAccount(payment);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
        return ResponseEntity.ok(payment.toString());
    }
}
