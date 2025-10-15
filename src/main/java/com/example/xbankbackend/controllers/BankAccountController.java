package com.example.xbankbackend.controllers;

import com.example.xbankbackend.models.BankAccount;
import com.example.xbankbackend.services.BankAccountService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@CrossOrigin
@RequestMapping("/api")
public class BankAccountController {
    @Autowired
    private BankAccountService bankAccountService;

    @PostMapping("/create/bankaccount")
    public String createBankAccount(@RequestBody BankAccount bankAccount) {
        try {
            bankAccountService.createBankAccount(bankAccount);
            log.info(bankAccount);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
        }
        return bankAccount.toString();
    }
}
