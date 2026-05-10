package com.example.xbankbackend.services.savings;

import com.example.xbankbackend.exceptions.BankAccountNotFoundException;
import com.example.xbankbackend.models.SavingsAccount;
import com.example.xbankbackend.repositories.SavingsAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@AllArgsConstructor
@Service
public class SavingsAccountValidationService {

    private final SavingsAccountRepository savingsAccountRepository;

    public void validateSavingsAccountExists(UUID accountId) {
        if (!savingsAccountRepository.exists(accountId)) {
            throw new BankAccountNotFoundException("Сберегательный счёт с UUID " + accountId + " не найден");
        }
    }

    public boolean validateSavingsAccountExistsSoft(UUID accountId) {
        return savingsAccountRepository.exists(accountId);
    }

    public void validateTopUpAllowed(UUID accountId) {
        SavingsAccount account = savingsAccountRepository.get(accountId);
        if (!account.isAllowTopUp()) {
            throw new AccessDeniedException("Пополнение запрещено для этого вклада");
        }
    }

    public void validateWithdrawalAllowed(UUID accountId) {
        SavingsAccount account = savingsAccountRepository.get(accountId);
        if (!account.isAllowWithdrawal()) {
            throw new AccessDeniedException("Снятие запрещено для этого вклада");
        }
    }
}
