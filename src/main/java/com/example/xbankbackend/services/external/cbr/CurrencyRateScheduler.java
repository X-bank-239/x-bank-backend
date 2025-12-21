package com.example.xbankbackend.services.external.cbr;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@AllArgsConstructor
@Component
@Log4j2
public class CurrencyRateScheduler {

    private CurrencyRateService currencyRateService;

    @Scheduled(cron = "0 10 0 * * ?")
    public void updateDailyRates() {
        LocalDate today = LocalDate.now();
        log.info("Updating currency rates for {}", today);
        currencyRateService.updateOnDate(today);
    }
}
