package com.example.xbankbackend.dtos.responses;

import com.example.xbankbackend.enums.BankAccountType;
import com.example.xbankbackend.enums.CurrencyType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BankAccountResponse {
    private BigDecimal amount;
    private CurrencyType currency;
    private BankAccountType accountType;
}
