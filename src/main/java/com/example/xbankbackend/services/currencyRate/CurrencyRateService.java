package com.example.xbankbackend.services.currencyRate;

import com.example.xbankbackend.dtos.requests.UpdateCurrencyRateRequest;
import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.exceptions.RateNotFoundException;
import com.example.xbankbackend.mappers.CurrencyMapper;
import com.example.xbankbackend.models.CurrencyRate;
import com.example.xbankbackend.repositories.CurrencyRateRepository;
import com.example.xbankbackend.services.external.cbr.CbrSoapService;
import com.example.xbankbackend.services.external.cbr.CurrencyParserService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
@Log4j2
public class CurrencyRateService {

    private CbrSoapService cbrSoapService;
    private CurrencyParserService currencyParserService;
    private CurrencyRateRepository currencyRateRepository;
    private CurrencyRateValidationService currencyRateValidationService;
    private CurrencyMapper currencyMapper;

    public void updateOnDate(LocalDate date) {
        String xmlResponse = cbrSoapService.getCursOnDate(date);
        Map<CurrencyType, BigDecimal> rates = currencyParserService.parseCurrencies(xmlResponse);

        for (Map.Entry<CurrencyType, BigDecimal> entry : rates.entrySet()) {
            if (currencyRateRepository.existsByCurrencyAndDate(entry.getKey(), date)) {
                log.info("Currency rate for {} of {} already exist, skipping update", date, entry.getKey());
                continue;
            }

            CurrencyRate rate = new CurrencyRate();
            rate.setCurrency(entry.getKey());
            rate.setRate(entry.getValue());
            rate.setDate(date);
            rate.setCreatedAt(OffsetDateTime.now());
            rate.setComment("[AUTO]");

            currencyRateRepository.create(rate);
        }

        log.info("Updated {} currency rates for {}: {}", rates.size(), date, rates);
    }

    public BigDecimal convert(CurrencyType from, CurrencyType to, BigDecimal amount) {
        BigDecimal fromRate = BigDecimal.ONE, toRate = BigDecimal.ONE;
        if (from != CurrencyType.RUB) {
            fromRate = currencyRateRepository.findLatestByCurrency(from).getRate();
        }
        if (to != CurrencyType.RUB) {
            toRate = currencyRateRepository.findLatestByCurrency(to).getRate();
        }
        return fromRate.multiply(amount).divide(toRate, 4, RoundingMode.HALF_UP);
    }

    public void createRate(CurrencyRate currencyRate) {
        currencyRateRepository.create(currencyRate);
    }

    public CurrencyRate updateRate(CurrencyType currency, LocalDate date, UpdateCurrencyRateRequest request) {
        currencyRateValidationService.validateCurrencyRateExistsByCurrencyAndDate(currency, date);

        CurrencyRate rate = currencyRateRepository.findByCurrencyAndDate(currency, date);
        currencyMapper.updateCurrencyFromRequest(request, rate);
        currencyRateRepository.update(currency, date, rate);

        return rate;
    }

    public void deleteRatesByDate(LocalDate date) {
        currencyRateValidationService.validateCurrencyRateExistsByDate(date);

        currencyRateRepository.deleteRatesByDate(date);
    }

    public List<CurrencyRate> getRatesByDate(LocalDate date) {
        currencyRateValidationService.validateCurrencyRateExistsByDate(date);

        return currencyRateRepository.findByDate(date);
    }

    public List<CurrencyRate> getLatestRates() {
        return currencyRateRepository.findByDateOrderByCurrencyAsc(LocalDate.now());
    }

    public CurrencyRate getLatestRateByCurrency(CurrencyType currency) {
        return currencyRateRepository.findLatestByCurrency(currency);
    }
}
