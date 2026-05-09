package com.example.xbankbackend.services.savings;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@AllArgsConstructor
@Component
@Log4j2
public class SavingsScheduler {

    private SavingsAccountService savingsAccountService;

    @Scheduled(cron = "0 0 23 * * ?")
    public void calculateDailyInterest() {
        LocalDate today = LocalDate.now();
        log.info("Calculating daily interest for {}", today);

        savingsAccountService.calculateDailyInterest();
    }

    @Scheduled(cron = "0 0 0 1 * ?")
    public void makeMonthlyAccrual() {
        LocalDate today = LocalDate.now();
        log.info("Calculating monthly accrual for {}", today);

        savingsAccountService.makeMonthlyAccrual();
    }
}
