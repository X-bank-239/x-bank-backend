package com.example.xbankbackend.services;

import com.example.xbankbackend.exceptions.UserNotFoundException;
import com.example.xbankbackend.models.BankAccount;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@Service
public class BankAccountService {

    private BankAccountRepository bankAccountRepository;
    private UserRepository userRepository;

    public void create(BankAccount bankAccount) {
        if (!userRepository.exists(bankAccount.getUserId())) {
            throw new UserNotFoundException("User with UUID " + bankAccount.getUserId() + " does not exist");
        }
        bankAccount.setBalance(BigDecimal.ZERO);
        bankAccount.setAccountId(UUID.randomUUID());
        bankAccountRepository.create(bankAccount);
    }
}
