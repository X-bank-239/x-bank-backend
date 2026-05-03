package com.example.xbankbackend.services.bankAccount;

import com.example.xbankbackend.exceptions.BankAccountNotFoundException;
import com.example.xbankbackend.exceptions.InsufficientFundsException;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.services.FeeService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@Service
public class BankAccountValidationService {

    private BankAccountRepository bankAccountRepository;
    private FeeService feeService;

    public void validateBankAccountExists(UUID accountId) {
        if (!bankAccountRepository.exists(accountId)) {
            throw new BankAccountNotFoundException("Счёт с UUID " + accountId + " не существует");
        }
    }

    public void validateBankAccountActive(UUID accountId) {
        if (!bankAccountRepository.isActive(accountId)) {
            throw new AccessDeniedException("Счёт с UUID " + accountId + " деактивирован");
        }
    }

    public void validateSufficientFundsWithFee(UUID accountId, BigDecimal amount) {
        BigDecimal balance = bankAccountRepository.getBalance(accountId);
        if (feeService.applyBaseFee(amount).compareTo(balance) >= 0) {
            throw new InsufficientFundsException("Баланс отправителя должен быть не меньше суммы транзакции");
        }
    }
}
