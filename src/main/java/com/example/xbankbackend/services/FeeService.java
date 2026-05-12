package com.example.xbankbackend.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@AllArgsConstructor
@Service
public class FeeService {

    private AppSettingsService settingsService;

    public BigDecimal applyBaseFee(BigDecimal amount) {
        BigDecimal baseFee = settingsService.getTransactionsBaseFee();

        return amount.multiply(BigDecimal.ONE.add(baseFee));
    }

    public BigDecimal applyFee(BigDecimal amount, Float fee) {
        return amount.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(fee)));
    }
    public BigDecimal getBaseFeeAmount(BigDecimal amount) {
       return applyBaseFee(amount).subtract(amount);
    }
}
