package com.example.xbankbackend.controllers;

import com.example.xbankbackend.models.BankAccount;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class CreateBankAccount {

    @PostMapping("/create/bankaccount")
    public String createBankAccount(@RequestBody BankAccount bankAccount) {
        System.out.println(bankAccount.getAmount());
        return bankAccount.getAmount().toString();
    }
}
