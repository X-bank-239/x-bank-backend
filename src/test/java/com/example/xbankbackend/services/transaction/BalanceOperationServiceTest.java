package com.example.xbankbackend.services.transaction;

import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.services.FeeService;
import com.example.xbankbackend.services.external.cbr.CurrencyRateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BalanceOperationService")
class BalanceOperationServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private CurrencyRateService currencyRateService;

    @InjectMocks
    private BalanceOperationService service;

    @Nested
    @DisplayName("increaseBalance")
    class IncreaseBalanceTests {

        @Test
        void shouldConvertCurrencyAndIncreaseBalance_WhenCurrenciesDiffer() {
            UUID accountId = UUID.randomUUID();
            BigDecimal amount = BigDecimal.valueOf(100);
            CurrencyType txCurrency = CurrencyType.USD;
            CurrencyType accountCurrency = CurrencyType.RUB;
            BigDecimal converted = BigDecimal.valueOf(9000);

            when(bankAccountRepository.getCurrency(accountId)).thenReturn(accountCurrency);
            when(currencyRateService.convert(txCurrency, accountCurrency, amount)).thenReturn(converted);

            service.increaseBalance(accountId, amount, txCurrency);

            verify(currencyRateService).convert(txCurrency, accountCurrency, amount);
            verify(bankAccountRepository).increaseBalance(accountId, converted);
        }

        @Test
        void shouldSkipConversion_WhenCurrenciesAreSame() {
            UUID accountId = UUID.randomUUID();
            BigDecimal amount = BigDecimal.valueOf(100);
            CurrencyType currency = CurrencyType.RUB;

            when(bankAccountRepository.getCurrency(accountId)).thenReturn(currency);
            when(currencyRateService.convert(currency, currency, amount)).thenReturn(amount);

            service.increaseBalance(accountId, amount, currency);

            verify(bankAccountRepository).increaseBalance(accountId, amount);
        }
    }

    @Nested
    @DisplayName("decreaseBalance")
    class DecreaseBalanceTests {

        @Test
        void shouldConvertCurrencyAndDecreaseBalance_WhenCurrenciesDiffer() {
            UUID accountId = UUID.randomUUID();
            BigDecimal amount = BigDecimal.valueOf(100);
            CurrencyType txCurrency = CurrencyType.EUR;
            CurrencyType accountCurrency = CurrencyType.RUB;
            BigDecimal converted = BigDecimal.valueOf(9500);

            when(bankAccountRepository.getCurrency(accountId)).thenReturn(accountCurrency);
            when(currencyRateService.convert(txCurrency, accountCurrency, amount)).thenReturn(converted);

            service.decreaseBalance(accountId, amount, txCurrency);

            verify(currencyRateService).convert(txCurrency, accountCurrency, amount);
            verify(bankAccountRepository).decreaseBalance(accountId, converted);
        }
    }
}
