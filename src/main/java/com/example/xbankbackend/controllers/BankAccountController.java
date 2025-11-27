package com.example.xbankbackend.controllers;

import com.example.xbankbackend.models.BankAccount;
import com.example.xbankbackend.services.BankAccountService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@CrossOrigin
@RequestMapping("/bankaccount")
public class BankAccountController {
    @Autowired
    private BankAccountService bankAccountService;

    @PostMapping("/create")
    public ResponseEntity<?> createBankAccount(@Valid @RequestBody BankAccount bankAccount) {
        log.info("Creating bank account: {}", bankAccount);
        bankAccountService.createBankAccount(bankAccount);
        return ResponseEntity.status(HttpStatus.CREATED).body(bankAccount);
    }
}
