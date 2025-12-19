package com.example.xbankbackend.services.external.cbr;

import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.models.external.cbr.CurrencyRate;
import com.example.xbankbackend.repositories.external.cbr.CurrencyRateRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@Service
@Log4j2
public class CurrencyRateService {

    private CbrSoapService cbrSoapService;
    private CurrencyParserService currencyParserService;
    private CurrencyRateRepository currencyRateRepository;

    public void updateOnDate(LocalDate date) {
        if (currencyRateRepository.existsByDate(date)) {
            log.info("Currency rates for {} already exist, skipping update", date);
            return;
        }

        String xmlResponse = cbrSoapService.getCursOnDate(date);
        Map<CurrencyType, Float> rates = currencyParserService.parseCurrencies(xmlResponse);

        for (Map.Entry<CurrencyType, Float> entry : rates.entrySet()) {
            CurrencyRate rate = new CurrencyRate();
            rate.setCurrency(entry.getKey());
            rate.setRate(entry.getValue());
            rate.setDate(date);
            rate.setCreatedAt(LocalDateTime.now());

            currencyRateRepository.create(rate);
        }

        log.info("Updated {} currency rates for {}: {}", rates.size(), date, rates);
    }
}
