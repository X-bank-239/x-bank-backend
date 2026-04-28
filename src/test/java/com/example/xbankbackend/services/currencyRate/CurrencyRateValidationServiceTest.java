package com.example.xbankbackend.services.currencyRate;

import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.exceptions.RateNotFoundException;
import com.example.xbankbackend.repositories.CurrencyRateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CurrencyRateValidationService")
class CurrencyRateValidationServiceTest {

    @Mock
    private CurrencyRateRepository currencyRateRepository;

    @InjectMocks
    private CurrencyRateValidationService service;

    @Nested
    @DisplayName("validateCurrencyRateExistsByDate")
    class ValidateCurrencyRateExistsByDateTests {

        @Test
        void shouldNotThrow_WhenRateExistsForDate() {
            LocalDate date = LocalDate.now();

            when(currencyRateRepository.existsByDate(date)).thenReturn(true);

            assertThatNoException().isThrownBy(() -> service.validateCurrencyRateExistsByDate(date));
        }

        @Test
        void shouldThrowRateNotFoundException_WhenRateNotExistsForDate() {
            LocalDate date = LocalDate.now();

            when(currencyRateRepository.existsByDate(date)).thenReturn(false);

            assertThatThrownBy(() -> service.validateCurrencyRateExistsByDate(date))
                    .isInstanceOf(RateNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("validateCurrencyRateExistsByCurrencyAndDate")
    class ValidateCurrencyRateExistsByCurrencyAndDateTests {

        @Test
        void shouldNotThrow_WhenRateExistsForCurrencyAndDate() {
            LocalDate date = LocalDate.now();
            CurrencyType currency = CurrencyType.USD;

            when(currencyRateRepository.existsByCurrencyAndDate(currency, date)).thenReturn(true);

            assertThatNoException().isThrownBy(() -> service.validateCurrencyRateExistsByCurrencyAndDate(currency, date));
        }

        @Test
        void shouldThrowRateNotFoundException_WhenRateNotExistsForCurrencyAndDate() {
            LocalDate date = LocalDate.now();
            CurrencyType currency = CurrencyType.EUR;

            when(currencyRateRepository.existsByCurrencyAndDate(currency, date)).thenReturn(false);

            assertThatThrownBy(() -> service.validateCurrencyRateExistsByCurrencyAndDate(currency, date))
                    .isInstanceOf(RateNotFoundException.class);
        }
    }
}
