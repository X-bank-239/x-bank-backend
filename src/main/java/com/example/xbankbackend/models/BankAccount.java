package com.example.xbankbackend.models;

import com.example.xbankbackend.enums.BankAccountType;
import com.example.xbankbackend.enums.CurrencyType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BankAccount {
    private UUID accountId;
    private UUID userId;
    private BigDecimal amount;
    private CurrencyType currency;
    private BankAccountType accountType;
}
