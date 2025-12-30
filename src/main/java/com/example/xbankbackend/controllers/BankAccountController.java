package com.example.xbankbackend.controllers;

import com.example.xbankbackend.dtos.requests.CreateBankAccountRequest;
import com.example.xbankbackend.jwt.SecurityUtil;
import com.example.xbankbackend.mappers.BankAccountMapper;
import com.example.xbankbackend.models.BankAccount;
import com.example.xbankbackend.services.BankAccountService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Log4j2
@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/bank-account")
public class BankAccountController {

    private BankAccountMapper bankAccountMapper;
    private BankAccountService bankAccountService;

    @PostMapping("/create")
    public ResponseEntity<BankAccount> create(@Valid @RequestBody CreateBankAccountRequest bankAccountRequest) {
        UUID userId = SecurityUtil.getCurrentUserId();

        log.info("Creating bank account: {}", bankAccountRequest);
        log.info("User ID: {}", userId);
        BankAccount bankAccount = bankAccountMapper.requestToAccount(bankAccountRequest);
        bankAccount.setUserId(userId);
        bankAccountService.create(bankAccount);
        return ResponseEntity.status(HttpStatus.CREATED).body(bankAccount);
    }

}
