package com.example.xbankbackend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class FeeService {

    @Value("${application.base-fee:0.015}")
    private Float baseFee;

    public BigDecimal applyBaseFee(BigDecimal amount) {
        return amount.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(baseFee)));
    }

    public BigDecimal applyFee(BigDecimal amount, Float fee) {
        return amount.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(fee)));
    }
    public BigDecimal getBaseFeeAmount(BigDecimal amount) {
       return applyBaseFee(amount).subtract(amount);
    }
}
