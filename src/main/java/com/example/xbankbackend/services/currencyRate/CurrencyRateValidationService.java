package com.example.xbankbackend.services.currencyRate;

import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.exceptions.RateNotFoundException;
import com.example.xbankbackend.repositories.CurrencyRateRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@AllArgsConstructor
@Service
public class CurrencyRateValidationService {

    private CurrencyRateRepository currencyRateRepository;

    public void validateCurrencyRateExistsByDate(LocalDate date) {
        if (!currencyRateRepository.existsByDate(date)) {
            throw new RateNotFoundException("Rate for date " + date + " not found");
        }
    }

    public void validateCurrencyRateExistsByCurrencyAndDate(CurrencyType currency, LocalDate date) {
        if (!currencyRateRepository.existsByCurrencyAndDate(currency, date)) {
            throw new RateNotFoundException("Rate for date " + date + " and currency " + currency + " not found");
        }
    }
}
