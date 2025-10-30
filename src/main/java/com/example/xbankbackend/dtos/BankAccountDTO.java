package com.example.xbankbackend.dtos;

import com.example.xbankbackend.enums.BankAccountType;
import com.example.xbankbackend.enums.CurrencyType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BankAccountDTO {
    private BigDecimal amount;
    private CurrencyType currency;
    private BankAccountType accountType;
}
