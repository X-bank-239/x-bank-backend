package com.example.xbankbackend.services;

import com.example.xbankbackend.exceptions.UserNotFoundException;
import com.example.xbankbackend.models.BankAccount;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BankAccountService {
    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private UserRepository userRepository;

    public void createBankAccount(BankAccount bankAccount) {
        if (!userRepository.haveUserId(bankAccount.getUserId())) {
            throw new UserNotFoundException("User with UUID " + bankAccount.getUserId() + " does not exist");
        }
        bankAccount.setBalance(0.0f);
        bankAccount.setAccountId(UUID.randomUUID());
        bankAccountRepository.createBankAccount(bankAccount);
    }
}
