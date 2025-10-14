package com.example.xbankbackend.controllers;

import com.example.xbankbackend.config.JOOQConfig;
import com.example.xbankbackend.models.BankAccount;
import org.jooq.DSLContext;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class BankAccountController {

    private final DSLContext dsl = JOOQConfig.createDSLContext();

    public BankAccountController() throws SQLException {
    }



    @PostMapping("/create/bankaccount")
    public String createBankAccount(@RequestBody BankAccount bankAccount) {
//        System.out.println(bankAccount.getAmount());


        return bankAccount.getAmount().toString();
    }
}
