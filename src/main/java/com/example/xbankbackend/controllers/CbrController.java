package com.example.xbankbackend.controllers;

import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.models.external.cbr.CurrencyRate;
import com.example.xbankbackend.services.external.cbr.CurrencyRateService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/cbr")
public class CbrController {

    private CurrencyRateService currencyRateService;

    @GetMapping("/rates/get-latest")
    public ResponseEntity<List<CurrencyRate>> getLatestRates() {
        log.info("Getting latest rates");

        List<CurrencyRate> rates = currencyRateService.getLatestRates();

        return ResponseEntity.status(HttpStatus.OK).body(rates);
    }

    @GetMapping("/rates/get-latest/{currency}")
    public ResponseEntity<CurrencyRate> getLatestRates(@PathVariable String currency) {
        log.info("Getting latest rate for {}", currency);

        CurrencyRate rate = currencyRateService.getLatestRateByCurrency(CurrencyType.valueOf(currency));

        return ResponseEntity.status(HttpStatus.OK).body(rate);
    }
}
