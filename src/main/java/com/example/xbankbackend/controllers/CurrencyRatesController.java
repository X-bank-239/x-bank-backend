package com.example.xbankbackend.controllers;

import com.example.xbankbackend.dtos.requests.CreateCurrencyRateRequest;
import com.example.xbankbackend.dtos.requests.UpdateCurrencyRateRequest;
import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.mappers.CurrencyMapper;
import com.example.xbankbackend.models.CurrencyRate;
import com.example.xbankbackend.services.currencyRate.CurrencyRateService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Log4j2
@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/currency-rates")
public class CurrencyRatesController {

    private CurrencyRateService currencyRateService;
    private CurrencyMapper currencyMapper;

    @GetMapping("/latest")
    public ResponseEntity<List<CurrencyRate>> getLatestRates() {
        log.info("Getting latest rates");

        List<CurrencyRate> rates = currencyRateService.getLatestRates();

        return ResponseEntity.status(HttpStatus.OK).body(rates);
    }

    @GetMapping("/latest/{currency}")
    public ResponseEntity<CurrencyRate> getLatestRates(@PathVariable String currency) {
        log.info("Getting latest rate for {}", currency);

        CurrencyRate rate = currencyRateService.getLatestRateByCurrency(CurrencyType.valueOf(currency));

        return ResponseEntity.status(HttpStatus.OK).body(rate);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<CurrencyRate>> getRatesByDate(@PathVariable LocalDate date) {
        log.info("Getting currencies for date {}", date);

        List<CurrencyRate> rates = currencyRateService.getRatesByDate(date);

        return ResponseEntity.status(HttpStatus.OK).body(rates);
    }

    @GetMapping("/supported")
    public ResponseEntity<CurrencyType[]> getSupportedCurrencies() {
        return ResponseEntity.status(HttpStatus.OK).body(CurrencyType.values());
    }

    // ADMIN-only

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CurrencyRate> createRate(@RequestBody @Valid CreateCurrencyRateRequest request) {
        log.info("[ADMIN] Creating rate {}", request);

        CurrencyRate currencyRate = currencyMapper.requestToCurrencyRate(request);
        currencyRateService.createRate(currencyRate);

        return ResponseEntity.status(HttpStatus.CREATED).body(currencyRate);
    }

    @PatchMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CurrencyRate> updateRate(@RequestParam CurrencyType currency,
                                                   @RequestParam LocalDate date,
                                                   @RequestBody @Valid UpdateCurrencyRateRequest request) {
        log.info("[ADMIN] Updating rate for {} on {}, {}", currency, date, request);

        CurrencyRate rate = currencyRateService.updateRate(currency, date, request);

        return ResponseEntity.status(HttpStatus.OK).body(rate);
    }

    @DeleteMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRatesByDate(@RequestParam LocalDate date) {
        log.info("[ADMIN] Deleting rates for {}", date);

        currencyRateService.deleteRatesByDate(date);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/sync-from-cbr")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> syncFromCbr(@RequestParam LocalDate date) {
        log.info("[ADMIN] Manual sync from cbr for {}", date);

        currencyRateService.updateOnDate(date);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
