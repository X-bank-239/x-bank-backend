package com.example.xbankbackend.controllers;

import com.example.xbankbackend.dtos.requests.CreateBankAccountRequest;
import com.example.xbankbackend.mappers.BankAccountMapper;
import com.example.xbankbackend.models.BankAccount;
import com.example.xbankbackend.services.BankAccountService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        log.info("Creating bank account: {}", bankAccountRequest);
        BankAccount bankAccount = bankAccountMapper.requestToAccount(bankAccountRequest);
        bankAccountService.create(bankAccount);
        return ResponseEntity.status(HttpStatus.CREATED).body(bankAccount);
    }

}
