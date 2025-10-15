package com.example.xbankbackend.services;

import com.example.xbankbackend.models.BankAccount;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class BankAccountService {
    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private UserRepository userRepository;

    public void createBankAccount(BankAccount bankAccount) {
        if (!userRepository.haveUUID(bankAccount.getUserId())) {
            throw new IllegalArgumentException("User with UUID " + bankAccount.getAccountId() + " does not exist");
        }
        bankAccount.setAmount(BigDecimal.ZERO);
        bankAccount.setAccountId(UUID.randomUUID());
        bankAccountRepository.createBankAccount(bankAccount);
    }
}
