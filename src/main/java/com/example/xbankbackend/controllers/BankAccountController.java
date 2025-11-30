package com.example.xbankbackend.controllers;

import com.example.xbankbackend.dtos.requests.CreateBankAccountRequest;
import com.example.xbankbackend.models.BankAccount;
import com.example.xbankbackend.services.BankAccountService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@Log4j2
@RestController
@CrossOrigin
@RequestMapping("/bank-account")
public class BankAccountController {

    private BankAccountService bankAccountService;

    @PostMapping("/create")
    public ResponseEntity<BankAccount> create(@Valid @RequestBody CreateBankAccountRequest bankAccountRequest) {
        log.info("Creating bank account: {}", bankAccountRequest);

        BankAccount bankAccount = convertRequest(bankAccountRequest);
        bankAccountService.create(bankAccount);
        return ResponseEntity.status(HttpStatus.CREATED).body(bankAccount);
    }

    private BankAccount convertRequest(CreateBankAccountRequest bankAccountRequest) {
        BankAccount bankAccount = new BankAccount();

        bankAccount.setUserId(bankAccountRequest.getUserId());
        bankAccount.setCurrency(bankAccountRequest.getCurrency());
        bankAccount.setAccountType(bankAccountRequest.getAccountType());

        return bankAccount;
    }
}
