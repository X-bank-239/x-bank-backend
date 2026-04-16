package com.example.xbankbackend.services.bankAccount;

import com.example.xbankbackend.exceptions.BankAccountNotFoundException;
import com.example.xbankbackend.repositories.BankAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@AllArgsConstructor
@Service
public class BankAccountValidationService {

    private BankAccountRepository bankAccountRepository;

    public void validateBankAccountExists(UUID accountId) {
        if (!bankAccountRepository.exists(accountId)) {
            throw new BankAccountNotFoundException("Account with UUID " + accountId + " does not exist");
        }
    }
}
