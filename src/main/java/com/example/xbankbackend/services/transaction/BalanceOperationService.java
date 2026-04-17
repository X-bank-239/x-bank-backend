package com.example.xbankbackend.services.transaction;

import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.services.FeeService;
import com.example.xbankbackend.services.external.cbr.CurrencyRateService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@Service
public class BalanceOperationService {

    private BankAccountRepository bankAccountRepository;
    private CurrencyRateService currencyRateService;
    private FeeService feeService;

    @Value("application.serviceAccountId")
    private UUID serviceAccountId;

    public void increaseBalance(UUID accountId, BigDecimal amount, CurrencyType txCurrency) {
        CurrencyType accountCurrency = bankAccountRepository.getCurrency(accountId);

        BigDecimal convertedAmount = currencyRateService.convert(txCurrency, accountCurrency, amount);

        bankAccountRepository.increaseBalance(accountId, convertedAmount);
    }

    public void decreaseBalance(UUID accountId, BigDecimal amount, CurrencyType txCurrency) {
        CurrencyType accountCurrency = bankAccountRepository.getCurrency(accountId);

        BigDecimal convertedAmount = currencyRateService.convert(txCurrency, accountCurrency, amount);

        bankAccountRepository.decreaseBalance(accountId, convertedAmount);
    }

    public void decreaseBalanceWithFee(UUID accountId, BigDecimal amount, CurrencyType txCurrency) {
        CurrencyType accountCurrency = bankAccountRepository.getCurrency(accountId);

        BigDecimal convertedAmount = currencyRateService.convert(txCurrency, accountCurrency, amount);
        BigDecimal amountWithFee = feeService.applyBaseFee(convertedAmount);
        BigDecimal fee = convertedAmount.subtract(amount);

        increaseBalance(serviceAccountId, fee, accountCurrency);
        bankAccountRepository.decreaseBalance(accountId, amountWithFee);
    }
}
